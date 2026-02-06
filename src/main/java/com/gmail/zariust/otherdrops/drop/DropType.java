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

package main.java.com.gmail.zariust.otherdrops.drop;

import main.java.com.gmail.zariust.common.CommonMaterial;
import main.java.com.gmail.zariust.common.Verbosity;
import main.java.com.gmail.zariust.otherdrops.*;
import main.java.com.gmail.zariust.otherdrops.data.Data;
import main.java.com.gmail.zariust.otherdrops.listener.OdSpawnListener;
import main.java.com.gmail.zariust.otherdrops.options.DoubleRange;
import main.java.com.gmail.zariust.otherdrops.subject.Agent;
import main.java.com.gmail.zariust.otherdrops.subject.PlayerSubject;
import main.java.com.gmail.zariust.otherdrops.subject.Target;
import main.java.com.gmail.zariust.otherdrops.things.ODItem;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public abstract class DropType {
    public enum DropCategory {
        ITEM, CREATURE, MONEY, GROUP, DENY, CONTENTS, DEFAULT, VEHICLE, EXPERIENCE, MYTHIC_CREATURE, ITEM_STACK
    }

    public static class DropFlags {
        protected final boolean naturally, spread, dropToInventory;
        protected final Random rng;
        protected final Player recipient;
        protected final String victim;
        protected final Agent tool;
        protected final String eventType;
        protected final String spawnReason;
        protected final Set<ODItem> dropsFilter;
        protected final boolean toKeepDrops;

        protected DropFlags(boolean d, boolean n, boolean s, Random ran, Player who, Agent tool, String eventType, String spawnReason, String victim, boolean toKeepDrops, Set<ODItem> dropsFilter) {
            dropToInventory = d;
            naturally = n;
            spread = s;
            rng = ran;
            recipient = who;
            this.tool = tool;
            this.eventType = eventType;
            this.spawnReason = spawnReason;
            this.victim = victim;
            this.toKeepDrops = toKeepDrops;
            this.dropsFilter = dropsFilter;
        }

        public String getEvent() {
            return eventType;
        }

        protected String getRecipientName() {
            if (recipient == null) return "";
            return recipient.getDisplayName();
        }

        protected String getToolName() {
            if (tool == null) return "";
            if (tool instanceof PlayerSubject) {
                return ((PlayerSubject) tool).getTool().getReadableName();
            }
            return tool.getReadableName();
        }
    }

    public boolean overrideDefault;
    private final DropCategory cat;
    private final double chance;
    // For MoneyDrop: Without this separate total, the amount dropped would increase every time if there is both an
    // embedded quantity and an external quantity. Moved into DropType as we need to make it available for messages
    public double total;
    protected String displayName;
    protected List<String> lore;
    public DropResult gDropResult;

    public DropType(DropCategory type) {
        this(type, 100.0);
    }

    public DropType(DropCategory type, double percent) {
        cat = type;
        chance = percent;
    }

    // Accessors
    public DropCategory getCategory() {
        return cat;
    }

    public double getChance() {
        return chance;
    }

    public static DropFlags flags(Player recipient, Agent tool, boolean dropToInventory, boolean naturally, boolean spread, Random rng, String eventType, String spawnReason, String victim) {
        return new DropFlags(dropToInventory, naturally, spread, rng, recipient, tool, eventType, spawnReason, victim, false, new HashSet<>());
    }

    public static DropFlags flags(Player recipient, Agent tool, boolean dropToInventory, boolean naturally, boolean spread, Random rng, String eventType, String spawnReason, String victim, boolean toKeepDrops, Set<ODItem> dropsFilter) {
        return new DropFlags(dropToInventory, naturally, spread, rng, recipient, tool, eventType, spawnReason, victim, toKeepDrops, dropsFilter);
    }

    // Drop now! Return false if the roll fails This is our initial point of entry for dropping This is a wrapper for the specific
    // droptype's "performDrop" - parse overall chance first then call performDrop "quantity" (from quantity: parameter) times over
    public DropResult drop(Location from, Target target, Location offset, double amount, DropFlags flags) {
        Location offsetLocation = calculateOffsetLocation(from, offset);

        if (chance < 100.0) {
            double rolledChance = flags.rng.nextDouble();
            Log.logInfo("Rolling chance: checking " + rolledChance + " <= " + (chance / 100) + " (" + (!(rolledChance > chance / 100.0)) + ")", Verbosity.HIGHEST);
            if (rolledChance > chance / 100.0) {
                Log.logInfo("Failed roll, returning...", Verbosity.HIGHEST);
                return DropResult.fromQuantity(-1);
            }
        }

        gDropResult = dropLocal(target, offsetLocation, amount, flags);
        return gDropResult;
    }

    private Location calculateOffsetLocation(Location from, Location offset) {
        if (offset != null) {
            offset.setWorld(from.getWorld()); // To avoid "differing world" errors
            return from.clone().add(offset);
        } else {
            return from.clone();
        }
    }

    // Exclusive Drop should call here to skip chance & offsetlocation note: exclusivedrop is a "chance distribution"
    // and chance values have already been checked, so skip if exclusivedrop
    protected DropResult dropLocal(Target target, Location where, double amount, DropFlags flags) {
        DropResult dropResult = new DropResult();
        int quantity = calculateQuantity(amount, flags.rng);
        // OtherDrops.logInfo("Calling performDrop...",Verbosity.HIGHEST);
        for (int i = 0; i < quantity; i++) {
            dropResult.add(performDrop(target, where, flags));
        }
        return dropResult;
    }

    // Methods to override!
    protected abstract DropResult performDrop(Target source, Location at, DropFlags flags);

    public abstract double getAmount();

    public abstract DoubleRange getAmountRange();

    protected abstract String getName();

    @Override
    public final String toString() {
        String result = getName();
        DoubleRange amount = getAmountRange();
        if (amount.getMin() != 1 || amount.getMax() != 1)
            result += "/" + (isQuantityInteger() ? amount.toIntRange() : amount);
        if (chance < 100 || chance > 100) result += "/" + chance + "%";
        return result;
    }

    protected int calculateQuantity(double amount, Random rng) {
        int intPart = (int) amount;
        if (amount - intPart >= 0.5) intPart = intPart + 1; // (int) discards the decimal place - round up if neccessary
        return intPart;
    }

    // Give a player an item!
    protected static DropResult drop(Player who, ItemStack stack, Location where, DropFlags flags) {
        DropResult dropResult = new DropResult();
        HashMap<Integer, ItemStack> notGiven = who.getInventory().addItem(stack);
        who.updateInventory();

        if (!notGiven.isEmpty()) {
            for (Integer key : notGiven.keySet()) {
                dropResult.addWithoutOverride(drop(where, notGiven.get(key), flags));
            }
        }

        dropResult.setQuantity(stack.getAmount());
        return dropResult;
    }

    // Drop an item!
    protected static DropResult drop(Location where, ItemStack stack, DropFlags flags) {
        boolean naturally = flags.naturally;
        Set<ODItem> dropsFilter = flags.dropsFilter;
        boolean toKeepDrops = flags.toKeepDrops;
        DropResult dropResult = new DropResult();
        boolean toDrop = false;

        if (stack.getType() == Material.AIR)
            return DropResult.fromQuantity(1); // don't want to crash clients with air item entities

        if (!dropsFilter.isEmpty()) {
            boolean found = false;
            for (ODItem odItem : dropsFilter) {
                if (odItem.matches(stack)) {
                    found = true;
                }
            }

            if (found) { // item was found in our content filter list
                if (toKeepDrops) toDrop = true; // contentskeep, only keep items found
            } else { // item was NOT found in our content filter list
                if (!toKeepDrops) toDrop = true; // contentsremove, since not in list lets drop it
            }
        }

        if (toDrop || dropsFilter.isEmpty()) {
            World in = where.getWorld();
            if (naturally) dropResult.addDropped(in.dropItemNaturally(where, stack));
            else dropResult.addDropped(in.dropItem(where, stack));

            dropResult.setQuantity(stack.getAmount());
        }
        return dropResult;
    }

    // Drop a creature!
    protected static DropResult drop(Location where, Player owner, EntityType type, Data data) {
        return dropCreatureWithRider(where, owner, type, data, null, null, "", "");
    }

    // Drop a MythicMob creature
    protected static DropResult drop(Location where, String mythicCreature) {
        DropResult dropResult = new DropResult();
        if (Dependencies.getMythicMobs().getMobManager().getMythicMob(mythicCreature).isPresent()) {
            MythicMob mob = Dependencies.getMythicMobs().getMobManager().getMythicMob(mythicCreature).orElse(null);
            if (mob != null) {
                ActiveMob activeMob = mob.spawn(BukkitAdapter.adapt(where), 1);
                Entity entity = activeMob.getEntity().getBukkitEntity();
                dropResult.addDropped(entity);
                dropResult.setQuantity(dropResult.getQuantity() + 1);
            }
        }
        return dropResult;
    }

    protected static DropResult dropCreatureWithRider(Location where, Player owner, EntityType type, Data data, CreatureDrop ride, Entity passenger, String eventName, String spawnReason) {
        if (spawnReason == null) spawnReason = "";

        DropResult dropResult = new DropResult();
        World in = where.getWorld();

        Log.dMsg("DROP MOB: spawnreason: " + spawnReason);
        // if this drop is due to a natural spawn, ensure the OD mob limit is not exceeeded
        if (owner == null && (spawnReason.isEmpty() || spawnReason.equalsIgnoreCase("natural")) && in.getLivingEntities().size() > OtherDropsConfig.globalCustomSpawnLimit) {
            Log.logInfo("Warning: cannot spawn mob as custom_spawn_limit (" + OtherDropsConfig.globalCustomSpawnLimit + ") exceeded (current count=" + in.getLivingEntities().size() + ").", Verbosity.HIGHEST);
            return dropResult;
        }
        Entity mob = null;

        Location spawnLoc = where.clone().add(new Location(where.getWorld(), 0.5, 0, 0.5));
        OdSpawnListener.otherdropsSpawned.clear(); // only used in on place (here) and only needs to store one entry
        if (!spawnReason.equals("odd")) {
            OdSpawnListener.otherdropsSpawned.put(OdSpawnListener.getSpawnLocKey(spawnLoc), type);
        }

        String mobSpawnError = "";
        try {
            mob = in.spawnEntity(spawnLoc, type);
        } catch (Exception e) {
            mobSpawnError = e.getLocalizedMessage();
        }
        try {
            if (mob == null) {
                CustomMobSupport.spawnCustomMob(type.toString(), spawnLoc);
            }
            data.setOn(mob, owner);
            mob.setMetadata("CreatureSpawnedBy", new FixedMetadataValue(OtherDrops.plugin, "OtherDrops"));
            dropResult.addDropped(mob);
            if (passenger != null) mob.addPassenger(passenger);

            if (ride != null) {
                dropResult.add(dropCreatureWithRider(where, owner, ride.getCreature(), ride.getData(), ride.getPassenger(), mob, eventName, spawnReason));
            }
            dropResult.setQuantity(1);
        } catch (Exception e) {
            Log.logInfo("DropType (entityspawn): failed to set entity data '" + type.toString() + "' at location: '" + spawnLoc + "' (reason: " + e.getLocalizedMessage() + ", " + mobSpawnError + ")", Verbosity.HIGH);
        }
        return dropResult;
    }

    @SuppressWarnings("rawtypes")
    public static DropType parseFrom(ConfigurationNode node) {
        Object drop = node.get("drop");
        String colour = OtherDropsConfig.getStringFrom(node, "color", "colour", "data");
        if (colour == null) colour = "0";
        if (drop == null) return null;
        else if (drop instanceof List list) {
            List<String> dropList = new ArrayList<>();
            for (Object obj : list)
                dropList.add(obj.toString());
            return DropListInclusive.parse(dropList, colour);
        } else if (drop instanceof Map map) {
            List<String> dropList = new ArrayList<>();
            for (Object obj : map.keySet())
                dropList.add(obj.toString());
            return DropListExclusive.parse(dropList, colour);
        } else if (drop instanceof Set set) { // Probably'll never happen, but whatever
            List<String> dropList = new ArrayList<>();
            for (Object obj : set)
                dropList.add(obj.toString());
            return DropListExclusive.parse(dropList, colour);
        } else return parse(drop.toString(), colour);
    }

    static String[] split(String drop) {
        String name, amount, chance, message = "";
        if (drop.contains("&/")) {
            drop = drop.replace("&/", "slashCharPlaceholder");
        }
        String[] split = drop.split("/");
        switch (split.length) {
            case 4:
                message = split[3];
            case 3:
                if (split[1].endsWith("%")) {
                    chance = split[1];
                    amount = split[2];
                } else {
                    chance = split[2];
                    amount = split[1];
                }
                break;
            case 2:
                if (split[1].endsWith("%")) {
                    chance = split[1];
                    amount = "";
                } else {
                    chance = "";
                    amount = split[1];
                }
                break;
            default:
                chance = amount = "";
        }
        name = split[0];
        if (chance.endsWith("%")) chance = chance.substring(0, chance.length() - 1);
        return new String[]{name, amount, chance, message};
    }

    public static DropType parse(String drop, String defaultData) {
        drop = CommonMaterial.substituteAlias(drop);

        String[] split = split(drop);
        String originalName = split[0];

        DoubleRange amount;
        try {
            amount = DoubleRange.parse(split[1]);
        } catch (IllegalArgumentException e) {
            amount = new DoubleRange(1.0, 1.0);
        }
        double chance;
        try {
            chance = Double.parseDouble(split[2]);
        } catch (NumberFormatException e) {
            chance = 100.0;
        }

        // Drop can be one of the following
        // - A Material constant, or one of the synonyms NOTHING and DYE
        // - A Material constant prefixed with VEHICLE_
        // - A EntityType constant prefixed with CREATURE_
        // - A MaterialGroup constant beginning with ANY_, optionally prefixed
        // with ^ to indicate ALL
        // - One of the special keywords DEFAULT, DENY, MONEY, CONTENTS
        if (originalName.toUpperCase().startsWith("ANY_")) {
            return DropListExclusive.parse(originalName, defaultData, amount.toIntRange(), chance);
        } else if (originalName.toUpperCase().startsWith("^ANY_") || originalName.toUpperCase().startsWith("EVERY_")) {
            return DropListInclusive.parse(originalName, defaultData, amount.toIntRange(), chance);
        } else {
            DropType dropType = CreatureDrop.parse(originalName, defaultData, amount.toIntRange(), chance);
            if (dropType != null) return dropType;

            if (originalName.toUpperCase().startsWith("VEHICLE_"))
                return VehicleDrop.parse(originalName, defaultData, amount.toIntRange(), chance);
            else if (originalName.toUpperCase().startsWith("MONEY")) return MoneyDrop.parse(originalName, defaultData, amount, chance);
            else if (originalName.toUpperCase().startsWith("MYTHIC_MOB@")) {
                return new MythicCreatureDrop(originalName.replaceAll("MYTHIC_MOB@", ""), amount.toIntRange(), chance);
            } else if (originalName.toUpperCase().startsWith("XP"))
                return ExperienceDrop.parse(originalName, defaultData, amount.toIntRange(), chance);
            else if (originalName.equalsIgnoreCase("CONTENTS")) return new ContentsDrop();
            else if (originalName.equalsIgnoreCase("DEFAULT")) return new ItemDrop((Material) null);
            else if (originalName.equalsIgnoreCase("THIS") || originalName.equalsIgnoreCase("SELF"))
                return new SelfDrop(amount.toIntRange(), chance);
            return ItemDrop.parse(originalName, defaultData, amount.toIntRange(), chance);
        }
    }

    public boolean isQuantityInteger() {
        return true;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String msg) {
        this.displayName = msg;

    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }
}
