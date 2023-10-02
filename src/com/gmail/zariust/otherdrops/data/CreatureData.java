// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.data;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.entities.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.gmail.zariust.common.Verbosity.EXTREME;

// Range only allowed for SHEEP, SLIME, and PIG_ZOMBIE
public class CreatureData implements Data, RangeableData {

    // Create a map of entity types against data objects
	private static final Map<EntityType, List<Class<?>>> DATAMAP;

    // Map of EntityTypes to new class based creature data, for ease of lookup
    // later on
    // note: there should be only one line per entity, or things could get messy
    static {
		Map<EntityType, List<Class<?>>> aMap = new HashMap<>();

        // Specific data
        put(aMap,"AXOLOTL", AxolotlData.class);
        put(aMap,"CAT", CatData.class);
        put(aMap,"CREEPER", CreeperData.class);
        put(aMap,"ENDERMAN", EndermanData.class);
        put(aMap, "FOX", FoxData.class);
        put(aMap, "FROG", FrogData.class);
        put(aMap, "HORSE", HorseData.class);
        put(aMap, "LLAMA", LlamaData.class);
        put(aMap, "PARROT", ParrotData.class);
        put(aMap, "ZOMBIFIED_PIGLIN", PigZombieData.class);
        put(aMap, "RABBIT", RabbitData.class);
        put(aMap, "SHEEP", SheepData.class);
        put(aMap, "SLIME", SlimeData.class);
        put(aMap, "MAGMA_CUBE", SlimeData.class);
        put(aMap, "VILLAGER", VillagerData.class);
        put(aMap, "WOLF", WolfData.class);

        // Scan through all entity types and if there's no current mapping
        // then check if it's an Ageable or LivingEntity and assign a mapping
        for (EntityType type : EntityType.values()) {
            Class<?> typeClass = type.getEntityClass();
            if (typeClass != null) {
                ArrayList<Class<?>> data = new ArrayList<>();
                if(aMap.containsKey(type))
                    data = new ArrayList<>(aMap.get(type));

                if (Ageable.class.isAssignableFrom(typeClass))
                    data.add(AgeableData.class);

                if (LivingEntity.class.isAssignableFrom(typeClass))
                    data.add(LivingEntityData.class);

                if (Tameable.class.isAssignableFrom(typeClass))
                    data.add(TameableData.class);

                String[] serverVersion = (Bukkit.getBukkitVersion().split("-")[0]).split("\\.");
                if(Integer.parseInt(serverVersion[0]) >= 1)
                    if(Integer.parseInt(serverVersion[1]) >= 16) {
                        Log.logInfo(ChatColor.RED + "Found server version " + serverVersion[0] + "." + serverVersion[1] + " >= 1.16, enabling steerable data!", Verbosity.HIGH);
                        if (Steerable.class.isAssignableFrom(typeClass))
                            data.add(SteerableData.class);
                    }

                aMap.put(type, data);
            }
        }
        DATAMAP = Collections.unmodifiableMap(aMap);
        Log.logInfo("CreatureData map: " + aMap, Verbosity.EXTREME);
    }
    public int                                  data;
    private Boolean                             sheared;


    private static void put(Map<EntityType, List<Class<?>>> aMap, String entityType, Class<?> classToAdd) {
        try {
            EntityType type = EntityType.valueOf(entityType);
            ArrayList<Class<?>> data = new ArrayList<>();
            if(aMap.containsKey(type))
                data = new ArrayList<>(aMap.get(type));
            data.add(classToAdd);
            aMap.put(type, data);
        } catch (IllegalArgumentException e) {
            Log.logInfo("Invalid entity found in CreatureData, could be older Minecraft version (can ignore): " + entityType, Verbosity.HIGHEST);
        }
    }

    public CreatureData(int mobData) {
        this(mobData, null);
    }

    public CreatureData(int mobData, Boolean sheared) {
        data = mobData;
        this.sheared = sheared;
    }

    public CreatureData() {
        this(0);
    }

    @Override
    public int getData() {
        return data;
    }

    @Override
    public void setData(int d) {
        data = d;
    }

    @Override
    public Boolean getSheared() {
        return sheared;
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof CreatureData))
            return false;
        // OtherDrops.logInfo("Checking data = "+data+" this.getsheared: "+sheared+" othersheared:"+d.getSheared());
        if (data == -2) { // for sheep with no color specified
            if (d.getSheared() == null)
                return true; // null is like a wildcard
            return sheared == d.getSheared();
        }

        boolean shearMatch = false;
        if (sheared == null)
            shearMatch = true;
        else if (sheared == d.getSheared())
            shearMatch = true;
        return (data == d.getData() && shearMatch);
    }

    @Override
    public String get(Enum<?> creature) {
        if (creature instanceof EntityType)
            return get((EntityType) creature);
        return "";
    }

    private String get(EntityType type) {
        Log.logInfo(
                "CreatureData: get("
                        + type.toString()
                        + "), shouldn't be here (should be in specific mob data) - please let developer know.",
                Verbosity.HIGH);
        return "";
    }

    @Override
    public void setOn(Entity mob, Player owner) {
        // nothing to do here, this code shouldn't be reached
        Log.logInfo(
                "CreatureData: setOn("
                        + mob.toString()
                        + ", "
                        + owner.toString()
                        + "), shouldn't be here (should be in specific mob data) - please let developer know.",
                Verbosity.HIGH);
    }

    @Override
    // No creature has a block state, so nothing to do here.
    public void setOn(BlockState state) {
    }

    public static Data parse(EntityType creature, String state) {
        // state = state.toUpperCase().replaceAll("[ _-]", "");

        if (DATAMAP.get(creature) != null) {
            CreatureData cData = null;
            try {
                MultipleEntityData multipleData = new MultipleEntityData();
                for(Class<?> classType : DATAMAP.get(creature)) {
                    multipleData.add((CreatureData) classType.getMethod("parseFromString", String.class).invoke(null, state));
                }
                cData = multipleData;
            } catch (IllegalArgumentException | NoSuchMethodException | InvocationTargetException |
                     IllegalAccessException | SecurityException e) {
                e.printStackTrace();
            }

            if (cData == null)
                return new CreatureData(0);
            return cData;

        } else {
            if (state == null || state.isEmpty())
                return new CreatureData(0);
        }
        return new CreatureData();
    }

    public static CreatureData parseFromString(String state) {
        Log.logInfo(
                "CreatureData: parseFromString("
                        + state
                        + "), shouldn't be here (should be in specific mob data) - please let developer know.",
                Verbosity.HIGH);
        return null;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        Log.logInfo(
                "CreatureData: parseFromEntity("
                        + entity.toString()
                        + "), shouldn't be here (should be in specific mob data) - please let developer know.",
                Verbosity.HIGH);
        return null;
    }

    @Override
    public String toString() {
        // TODO: Should probably make sure this is not used, and always use the
        // get method instead
        Log.logWarning("CreatureData.toString() was called! Is this right?", EXTREME);
        Log.stackTrace();
        return String.valueOf(data);
    }

    @Override
    public int hashCode() {
        return data;
    }

    public static Data parse(Entity entity) {
        if (entity == null)
            return new CreatureData(0);
        EntityType creatureType = entity.getType();
        if (creatureType == null)
            return new CreatureData(0);

        if (DATAMAP.get(entity.getType()) != null) {
            CreatureData cData = null;
            try {
                MultipleEntityData multipleData = new MultipleEntityData();
                for(Class<?> classType : DATAMAP.get(entity.getType())) {
                    multipleData.add((CreatureData) classType.getMethod("parseFromEntity", Entity.class).invoke(null, entity));
                }
                cData = multipleData;
            } catch (IllegalArgumentException | SecurityException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (cData == null)
                return new CreatureData(0);

            return cData;
        }
        return new CreatureData(0);
    }

    public static Data parse(EntityType mob, int mobData) {
        return CreatureData.parse(mob, String.valueOf(mobData));
    }
}
