package main.java.com.gmail.zariust.otherdrops.data.entities;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Stray;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.ItemStack;

import main.java.com.gmail.zariust.common.Verbosity;
import main.java.com.gmail.zariust.otherdrops.EntityWrapper;
import main.java.com.gmail.zariust.otherdrops.Log;
import main.java.com.gmail.zariust.otherdrops.OtherDropsConfig;
import main.java.com.gmail.zariust.otherdrops.data.CreatureData;
import main.java.com.gmail.zariust.otherdrops.data.Data;
import main.java.com.gmail.zariust.otherdrops.drop.ItemDrop;
import main.java.com.gmail.zariust.otherdrops.options.IntRange;
import main.java.com.gmail.zariust.otherdrops.things.ODVariables;

public class LivingEntityData extends CreatureData {
    final Double maxHealth;
    final CreatureEquipment equip;
    final String customName;

    public LivingEntityData(Double maxHealth, CreatureEquipment equip, String customName) {
        this.maxHealth = maxHealth;
        this.equip = equip;
        this.customName = ODVariables.preParse(customName);
    }

    @Override
    public void setOn(Entity mob, Player owner) {
        if (mob instanceof LivingEntity z) {

            // Maxhealth wrapped in a try/catch so equipment setting below will
            // continue even if setting the max health fails.
            // (maxhealth failed with some values on an older version of Bukkit)
            try {
                if (maxHealth != null) {
                    EntityWrapper.setMaxHealth(z, maxHealth);
                    EntityWrapper.setHealth(z, maxHealth);
                }
            } catch (Exception ignored) {
            }

            if (equip != null) {
                if (equip.head != null) z.getEquipment().setHelmet(equip.head);
                if (equip.headChance != null) z.getEquipment().setHelmetDropChance(equip.headChance);
                if (equip.handsMain != null) z.getEquipment().setItemInMainHand(equip.handsMain);
                if (equip.handsOff != null) z.getEquipment().setItemInOffHand(equip.handsOff);
                if (equip.handsMainChance != null) z.getEquipment().setItemInMainHandDropChance(equip.handsMainChance);
                if (equip.handsOffChance != null) z.getEquipment().setItemInOffHandDropChance(equip.handsOffChance);
                if (equip.chest != null) z.getEquipment().setChestplate(equip.chest);
                if (equip.chestChance != null) z.getEquipment().setChestplateDropChance(equip.chestChance);
                if (equip.legs != null) z.getEquipment().setLeggings(equip.legs);
                if (equip.legsChance != null) z.getEquipment().setLeggingsDropChance(equip.legsChance);
                if (equip.boots != null) z.getEquipment().setBoots(equip.boots);
                if (equip.bootsChance != null) z.getEquipment().setBootsDropChance(equip.bootsChance);

            } else {
                setDefaultEq((LivingEntity) mob);
            }

            // not currently used - refer to CreatureDrop instead
            if (customName != null) {
                String parsedCustomName = new ODVariables().setPlayerName(owner.getName()).parse(customName);
                z.setCustomName(parsedCustomName);
            }
        }
    }

    private void setDefaultEq(LivingEntity mob) {
        if (mob instanceof Skeleton skellie) {
            if (equip == null || equip.handsMain == null)
                skellie.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
        }
        if (mob instanceof Stray skellie) {
            if (equip == null || equip.handsMain == null)
                skellie.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
        }
        if (mob instanceof WitherSkeleton skellie) {
            if (equip == null || equip.handsMain == null) {
                skellie.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
            }
        }
    }

    @Override
    public boolean matches(Data d) {
        boolean match = true;
        if (!(d instanceof LivingEntityData vd)) {
            Log.logInfo("Checking LivingEntityData: target data not LivingEntityData. d=" + d.toString() + " (type: " + d.getClass().getName() + ")", Verbosity.EXTREME);
            match = false;
        } else {
            if (this.maxHealth != null) {
                if (!this.maxHealth.equals(vd.maxHealth)) {
                    Log.logInfo("Checking LivingEntityData: maxHealth failed.", Verbosity.EXTREME);
                    match = false;
                }
            }
            // compare equipment
            if (this.equip != null) {
                if (!this.equip.matches(vd.equip)) {
                    Log.logInfo("Checking LivingEntityData: equipment failed.", Verbosity.EXTREME);
                    match = false;
                }
            }
            if (this.customName != null) {
                if (this.customName.equals("CoolLordsWayToEnsureNobodyUsesThisNameHAHA")) {
                    if (!(vd.customName == null)) { // this means the mob has a name, so fail
                        Log.logInfo("Checking LivingEntityData: customname1 failed.", Verbosity.EXTREME);
                        match = false;
                    }
                } else if (this.customName.equals("*")) {
                    // * is a wildcard = match any name (except none) so fail if no mob name
                    if (vd.customName == null) {
                        Log.logInfo("Checking LivingEntityData: customname2 failed.", Verbosity.EXTREME);
                        match = false;
                    }
                } else if (vd.customName == null) {
                    Log.logInfo("Checking LivingEntityData: customname3 failed.", Verbosity.EXTREME);
                    match = false;
                } else if (!vd.customName.equals(this.customName)) {
                    Log.logInfo("Checking LivingEntityData: customname4 failed.", Verbosity.EXTREME);
                    match = false;
                }
            }
        }
        return match;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof LivingEntity livEnt) {
            return new LivingEntityData(EntityWrapper.getMaxHealth(livEnt).getValue(), CreatureEquipment.parseFromEntity(entity), entity.getCustomName());
        } else {
            Log.logInfo("LivingEntityData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }
    }

    public static CreatureData parseFromString(String state) {
        Double maxHealth = null;
        CreatureEquipment equip = null;
        String customName = null;
        String newState;

        if (!state.isEmpty() && !state.equals("0")) {
            String[] customNameSplit = state.split("~", 2);
            newState = customNameSplit[0];
            if (customNameSplit.length > 1) customName = customNameSplit[1];

            String[] split = newState.split(OtherDropsConfig.CreatureDataSeparator);

            for (String sub : split) {
                if (sub.matches("(?i)[0-9.]+hp?")) {
                    maxHealth = Double.valueOf(sub.replaceAll("[^0-9.]", ""));
                } else {
                    sub = sub.replaceAll("[\\s-_]", "");
                    if (sub.matches("(?i)eq:.*")) {
                        if (equip == null) equip = new CreatureEquipment();
                        equip = parseEquipmentString(sub, equip);
                    }
                }
            }
        }
        if (customName == null && (state.contains("~"))) customName = "CoolLordsWayToEnsureNobodyUsesThisNameHAHA";
        return new LivingEntityData(maxHealth, equip, customName);
    }

    private static CreatureEquipment parseEquipmentString(String sub, CreatureEquipment passEquip) {
        String[] subSplit = sub.split(":", 3);

        if (subSplit.length == 3) {
            String[] split = subSplit[2].split("%"); // split out the drop chance, if any
            String slot = split[0];
            float chance = 100; // default to 100% drop chance
            if (split.length > 1) {
                chance = Float.parseFloat(split[1]) / 100;
            }

            if (subSplit[1].matches("(?i)(head|helmet)")) {
                passEquip.head = getItemStack(slot);
                passEquip.headChance = chance;
            } else if (subSplit[1].matches("(?i)(mainhand)")) {
                passEquip.handsMain = getItemStack(slot);
                passEquip.handsMainChance = chance;
            } else if (subSplit[1].matches("(?i)(offhand)")) {
                passEquip.handsOff = getItemStack(slot);
                passEquip.handsOffChance = chance;
            } else if (subSplit[1].matches("(?i)(chest|chestplate|body)")) {
                passEquip.chest = getItemStack(slot);
                passEquip.chestChance = chance;
            } else if (subSplit[1].matches("(?i)(legs|leggings|legplate)")) {
                passEquip.legs = getItemStack(slot);
                passEquip.legsChance = chance;
            } else if (subSplit[1].matches("(?i)(feet|boots)")) {
                passEquip.boots = getItemStack(slot);
                passEquip.bootsChance = chance;
            }
        }
        return passEquip;

    }

    private static ItemStack getItemStack(String slot) {

        // this section doesn't work yet - need to save a list of itemstacks and
        // choose one at spawn time
        /*
         * if (slot.startsWith("any")) { // material group
         * Log.logInfo("Checking materialgroup..."); MaterialGroup group =
         * MaterialGroup.get(slot); if (group != null) { Material mat =
         * group.getOneRandom(); if (mat != null) {
         * Log.logInfo("Checking materialgroup...MAT = "+mat.toString()); return
         * new ItemStack(mat); } } } else {
         */
        ItemDrop item = (ItemDrop) ItemDrop.parse(slot, "", new IntRange(1), 100);
        if (item != null) return item.getItem();
        // }

        return null;
    }

    @Override
    public String toString() {
        String val = "";
        if (equip != null) {
            val += "!!" + equip;
        }
        if (maxHealth != null) {
            val += "%" + maxHealth + "h";
        }
        return val;
    }

    @Override
    public String get(Enum<?> creature) {
        if (creature instanceof EntityType) return this.toString();
        return "";
    }
}
