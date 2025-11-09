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

package com.gmail.zariust.common;

import com.gmail.zariust.otherdrops.Log;
import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;

import java.util.*;

public class CreatureGroup {
    private static final Map<String, CreatureGroup> lookup = new HashMap<>();

    private final ArrayList<EntityType> mob = new ArrayList<>();
    private final String name;

    private CreatureGroup(String name, List<EntityType> mob) {
        this.name = name;
        this.mob.addAll(mob);
    }

    private CreatureGroup(String name, String... entities) {
        this.name = name;
        for (String ent : entities) {
            try {
                EntityType entity = EntityType.valueOf(ent);
                this.mob.add(entity);
            } catch (IllegalArgumentException e) {
                Log.logInfo("Invalid entity found in CreatureGroup, could be older Minecraft version (can ignore): " + ent, Verbosity.HIGHEST);
            }
        }
    }

    public static CreatureGroup register(String name, String... types) {
        CreatureGroup group = new CreatureGroup(name, types);
        lookup.put(name.toUpperCase(), group);
        return group;
    }

    public static CreatureGroup register(String name, List<EntityType> types) {
        CreatureGroup group = new CreatureGroup(name, types);
        lookup.put(name.toUpperCase(), group);
        return group;
    }

    @Override
    public String toString() {
        return name;
    }

    public static final CreatureGroup CREATURE_ANY = register("CREATURE_ANY");

    static {
        register("CREATURE_HOSTILE", "BLAZE", "CREEPER", "ELDER_GUARDIAN", "ENDER_DRAGON", "ENDERMITE", "EVOKER", "DROWNED", "GHAST", "GIANT", "GUARDIAN", "HOGLIN", "HUSK", "ILLUSIONER", "MAGMA_CUBE", "PHANTOM", "PIGLIN_BRUTE", "PILLAGER", "RAVAGER", "SHULKER", "SILVERFISH", "SKELETON", "SLIME", "STRAY", "VEX", "VINDICATOR", "WARDEN", "WITCH", "WITHER", "WITHER_SKELETON", "ZOGLIN", "ZOMBIE", "ZOMBIE_VILLAGER");
        register("CREATURE_FRIENDLY", "ALLAY", "BAT", "CAMEL", "CAT", "CHICKEN", "COD", "COW", "DONKEY", "FOX", "FROG", "HORSE", "MUSHROOM_COW", "MULE", "OCELOT", "PARROT", "PIG", "PUFFERFISH", "RABBIT", "SHEEP", "SALMON", "SKELETON_HORSE", "SNIFFER", "SNOWMAN", "SQUID", "STRIDER", "TADPOLE", "TROPICAL_FISH", "TURTLE", "VILLAGER", "WANDERING_TRADER", "ZOMBIE_HORSE");
        register("CREATURE_NEUTRAL", "BEE", "DOLPHIN", "ENDERMAN", "GOAT", "IRON_GOLEM", "LLAMA", "PANDA", "PIGLIN", "POLAR_BEAR", "WOLF", "ZOMBIFIED_PIGLIN");
        register("CREATURE_ANIMAL", "AXOLOTL", "BAT", "BEE", "CAMEL", "CAT", "COD", "COW", "CHICKEN", "DOLPHIN", "DONKEY", "FOX", "FROG", "GLOW_SQUID", "HORSE", "LLAMA", "MULE", "MUSHROOM_COW", "OCELOT", "PANDA", "PARROT", "PIG", "POLAR_BEAR", "PUFFERFISH", "RABBIT", "SALMON", "SHEEP", "SNIFFER", "SQUID", "TADPOLE", "TROPICAL_FISH", "TURTLE", "WOLF");
        register("CREATURE_UNDEAD", "DROWNED", "ENDERMAN", "HUSK", "PHANTOM", "SKELETON", "STRAY", "WITHER_SKELETON", "ZOGLIN", "ZOMBIE", "ZOMBIE_VILLAGER", "ZOMBIFIED_PIGLIN");
        register("CREATURE_BUG", "BEE", "CAVE_SPIDER", "ENDERMITE", "SILVERFISH", "SPIDER");
        register("CREATURE_WATER", "AXOLOTL", "COD", "DOLPHIN", "ELDER_GUARDIAN", "GLOW_SQUID", "GUARDIAN", "PUFFERFISH", "SALMON", "SQUID", "TADPOLE", "TROPICAL_FISH", "TURTLE");
        register("CREATURE_BOSS", "ENDER_DRAGON", "WITHER");
        register("CREATURE_NETHER", "BLAZE", "CHICKEN", "ENDERMAN", "GHAST", "HOGLIN", "MAGMA_CUBE", "PIGLIN", "PIGLIN_BRUTE", "SKELETON", "STRIDER", "WITHER_SKELETON", "ZOMBIFIED_PIGLIN");
        register("CREATURE_END", "ENDER_DRAGON", "ENDERMAN", "ENDERMITE", "SHULKER");
        register("CREATURE_ANY", List.of(EntityType.values()));
        registerTagMaterials();
    }

    private static void registerTagMaterials() {
        Iterable<Tag<EntityType>> tags = Bukkit.getTags("entity_types", EntityType.class);
        for (Tag<EntityType> tag : tags) {
            if (tag.getValues().isEmpty()) continue;
            try {
                String name = "CREATURE_TAG_" + tag.getKey().toString().toUpperCase().replace("MINECRAFT:", "");
                register(name, List.copyOf(tag.getValues()));
            } catch (Exception e) {
                Log.logWarning("Failed to register tag group: " + tag.getKey());
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<EntityType> creatures() {
        return (List<EntityType>) mob.clone();
    }

    public static CreatureGroup get(String string) {
        return lookup.get(string.toUpperCase());
    }

    public static Set<String> all() {
        return lookup.keySet();
    }

    public static boolean isValid(String string) {
        return lookup.containsKey(string);
    }

    public boolean contains(EntityType material) {
        return mob.contains(material);
    }
}
