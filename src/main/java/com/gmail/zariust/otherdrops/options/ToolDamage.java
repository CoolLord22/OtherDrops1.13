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

package com.gmail.zariust.otherdrops.options;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.things.ODItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Random;

import static com.gmail.zariust.otherdrops.data.ItemData.getDurability;
import static com.gmail.zariust.otherdrops.data.ItemData.getMaxDurability;

public class ToolDamage {
    private ShortRange durabilityRange;
    private IntRange consumeRange;
    private ODItem replaceItem;
    private IntRange replaceItemQuantity;

    public ToolDamage() {
        this(null, 1);
    }

    public ToolDamage(Integer damage) {
        this(damage, 1);
    }

    public ToolDamage(Integer damage, int replaceQuantity) {
        if (damage != null) durabilityRange = ShortRange.parse(String.valueOf(damage));
        this.replaceItemQuantity = new IntRange(replaceQuantity);
    }

    public ItemStack apply(ItemStack original, Random rng) {
        ItemStack stack = original.clone();
        boolean fullyConsumed = false;
        short maxDurability = (short) getMaxDurability(stack);

        // 1. durability (damagetool / fixtool)
        if (maxDurability > 0 && durabilityRange != null) {
            short durability = (short) getDurability(stack);
            short damage = durabilityRange.getRandomIn(rng);
            fullyConsumed = setDurability(stack, maxDurability, (short) (durability + damage), rng);
        }

        // 2. stack size (consumetool / growtool)
        if (consumeRange != null && (fullyConsumed || durabilityRange == null || maxDurability <= 0)) {
            if (fullyConsumed) {
                fullyConsumed = false;
                resetDurability(stack);
            }
            int count = stack.getAmount();
            int take = consumeRange.getRandomIn(rng);
            if (count <= take) fullyConsumed = true;
            else stack.setAmount(count - take);
            Log.logInfo("Tool consume: " + take + "x " + stack + " consumed (" + (count - take) + ") remaining.", Verbosity.HIGH);
        }

        // 3. replacement (replacetool): when it has been fully used up, or when replacetool is the only option
        if (replaceItem != null && (fullyConsumed || (durabilityRange == null && consumeRange == null))) {
            ItemStack replacement = replaceItem.createStack(replaceItemQuantity);
            if (replacement != null) return replacement;
            Log.logWarning("replacetool: couldn't resolve '" + replaceItem + "'; tool not replaced.");
        }

        return fullyConsumed ? null : stack;
    }

    private void resetDurability(ItemStack stack) {
        if (stack.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(0);
            stack.setItemMeta(damageable);
        }
    }

    private boolean setDurability(ItemStack stack, short maxDamage, short durability, Random rng) {
        boolean fullyConsumed = false;

        if (!(stack.getItemMeta() instanceof Damageable damageable)) return fullyConsumed;
        if (stack.getItemMeta().isUnbreakable()) return fullyConsumed;

        if (stack.containsEnchantment(Enchantment.DURABILITY)) {
            int durabilityLevel = (stack.getEnchantmentLevel(Enchantment.DURABILITY) + 1);
            double chanceOfDamage = (double) 100 / (durabilityLevel);

            String name = stack.getType().toString().toLowerCase();
            if (name.contains("_helmet") || name.contains("_chestplate") || name.contains("_leggings") || name.contains("_boots"))
                chanceOfDamage = 60 + ((double) 40 / durabilityLevel);

            int n = rng.nextInt(100) + 1;

            if (n > chanceOfDamage) {
                Log.logInfo("Tool with unbreaking failed damage (expected behavior).", Verbosity.HIGH);
                return fullyConsumed;
            }
        }
        if (durability >= maxDamage) {
            durability = maxDamage;
            fullyConsumed = true;
        } else if (durability < 0) {
            durability = 0;       // fixtool can't repair past "brand new"
        }
        Log.logInfo("Tool damaged.", Verbosity.HIGH);
        damageable.setDamage(durability);
        stack.setItemMeta(damageable);
        return fullyConsumed;
    }

    public static ToolDamage parseFrom(ConfigurationNode node) {
        ToolDamage damage = new ToolDamage();
        // Durability
        String durability = node.getString("damagetool");
        if (durability != null) damage.durabilityRange = ShortRange.parse(durability);
        else {
            durability = node.getString("fixtool");
            if (durability != null) {
                ShortRange range = ShortRange.parse(durability);
                damage.durabilityRange = range.negate(range);
            }
        }
        // Amount
        String consume = node.getString("consumetool");
        if (consume != null) damage.consumeRange = IntRange.parse(consume);
        else {
            consume = node.getString("growtool");
            if (consume != null) {
                IntRange range = IntRange.parse(consume);
                damage.consumeRange = range.negate(range);
            }
        }
        // Replace
        String replace = node.getString("replacetool");
        if (replace != null) {
            String[] replaceSplit = replace.split("/q#");
            if (replaceSplit.length > 1) damage.replaceItemQuantity = IntRange.parse(replaceSplit[1]);
            damage.replaceItem = ODItem.parseItem(replace.replaceAll("(\\/q#\\d{1,9}-\\d{1,9}|\\/q#\\d{1,9})", ""));

            Log.logInfo("...tool will be replaced by " + damage.replaceItem, Verbosity.NORMAL);
        }
        if (damage.durabilityRange != null || damage.consumeRange != null || damage.replaceItem != null) return damage;
        return null;
    }

    public boolean isReplacement() {
        return this.replaceItem != null;
    }
    public boolean isDamage() {
        return this.durabilityRange != null;
    }

    @Override
    public String toString() {
        return "{" + "damage: " + durabilityRange + "quantity: " + consumeRange + "replace: " + replaceItem + "}";
    }
}
