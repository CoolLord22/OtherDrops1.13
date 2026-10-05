package com.gmail.zariust.otherdrops.data.mob.traits;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.EntityWrapper;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.config.ConfigSubject;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.item.ODItem;
import com.gmail.zariust.otherdrops.data.mob.CreatureData;
import com.gmail.zariust.otherdrops.data.mob.CreatureEquipment;
import com.gmail.zariust.otherdrops.things.ODVariables;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

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

        if (!state.isEmpty() && !state.equals("0")) {
            String[] customNameSplit = state.split("~", 2);
            if (customNameSplit.length > 1) customName = customNameSplit[1];
            for (String token : CreatureData.tokens(state)) {
                if (token.matches("(?i)[0-9.]+hp?")) {
                    maxHealth = Double.valueOf(token.replaceAll("[^0-9.]", ""));
                } else if (token.toLowerCase().startsWith("eq:")) {
                    if (equip == null) equip = new CreatureEquipment();
                    parseEquipmentString(token.replaceAll("\\s", ""), equip);
                }
            }
        }
        if (customName == null && state.contains("~")) customName = "CoolLordsWayToEnsureNobodyUsesThisNameHAHA";
        return new LivingEntityData(maxHealth, equip, customName);
    }

    private static void parseEquipmentString(String sub, CreatureEquipment passEquip) {
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

    }

    /** An equipment item: any item string, including saved items (Ex: DIAMOND_HELMET@protection#4, OD_ITEM@key). */
    private static ItemStack getItemStack(String slot) {
        ODItem item = ConfigSubject.parseSubject(slot).getODItem();
        if (item == null) {
            Log.logWarning("Invalid equipment item '" + slot + "'; skipping...");
            return null;
        }
        return item.create(1, null, null);
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