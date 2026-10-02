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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

import static com.gmail.zariust.otherdrops.data.ItemData.getDurability;
import static com.gmail.zariust.otherdrops.data.ItemData.getMaxDurability;

public class ToolDamage {
    private static final String LOG = "ToolDamage: ";

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

    /**
     * Applies tool damage/consumption/replacement to a COPY of the tool.
     * Returns the new tool (modified copy or replacement), or null if it's used up and should be removed.
     */
    public ItemStack apply(ItemStack original, Random rng) {
        ItemStack stack = original.clone();
        boolean fullyConsumed = false;
        short maxDurability = (short) getMaxDurability(stack);

        // 1. durability (damagetool / fixtool)
        if (durabilityRange != null) {
            if (maxDurability > 0) {
                short durability = (short) getDurability(stack);
                short damage = durabilityRange.getRandomIn(rng);
                fullyConsumed = setDurability(stack, maxDurability, durability, (short) (durability + damage), rng);
            } else {
                Log.logInfo(LOG + describe(stack) + " has no durability; damagetool/fixtool skipped.", Verbosity.HIGH);
            }
        }

        // 2. stack size (consumetool / growtool)
        if (consumeRange != null && (fullyConsumed || durabilityRange == null || maxDurability <= 0)) {
            if (fullyConsumed) {
                fullyConsumed = false;
                resetDurability(stack);
                Log.logInfo(LOG + "tool broke; taking from the stack instead (durability reset for the next item).", Verbosity.HIGH);
            }
            int count = stack.getAmount();
            int take = consumeRange.getRandomIn(rng);
            if (take < 0) {
                stack.setAmount(count - take);
                Log.logInfo(LOG + "grew " + describeName(stack) + " by " + (-take) + " (" + count + " -> " + stack.getAmount() + ").", Verbosity.HIGH);
            } else if (count <= take) {
                fullyConsumed = true;
                Log.logInfo(LOG + "used up " + describeName(stack) + " (had " + count + ", consumed " + take + ").", Verbosity.HIGH);
            } else {
                stack.setAmount(count - take);
                Log.logInfo(LOG + "consumed " + take + "x " + describeName(stack) + " (" + count + " -> " + stack.getAmount() + ").", Verbosity.HIGH);
            }
        }

        // 3. replacement (replacetool): when it has been fully used up, or when replacetool is the only option
        if (replaceItem != null && (fullyConsumed || (durabilityRange == null && consumeRange == null))) {
            ItemStack replacement = replaceItem.createStack(replaceItemQuantity);
            if (replacement != null) {
                Log.logInfo(LOG + "replaced " + describe(original) + " with " + describe(replacement) + ".", Verbosity.HIGH);
                return replacement;
            }
            Log.logWarning(LOG + "replacetool couldn't resolve '" + replaceItem + "'; tool not replaced.");
        }

        return fullyConsumed ? null : stack;
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
        }
        if (damage.durabilityRange != null || damage.consumeRange != null || damage.replaceItem != null) {
            Log.logInfo(LOG + "loaded " + damage, Verbosity.HIGHEST);
            return damage;
        }
        return null;
    }

    private boolean setDurability(ItemStack stack, short maxDamage, short oldDamage, short newDamage, Random rng) {
        if (!(stack.getItemMeta() instanceof Damageable damageable)) {
            Log.logInfo(LOG + describe(stack) + " isn't damageable; damagetool/fixtool skipped.", Verbosity.HIGHEST);
            return false;
        }
        if (damageable.isUnbreakable()) {
            Log.logInfo(LOG + describe(stack) + " is unbreakable; no damage applied.", Verbosity.HIGHEST);
            return false;
        }

        boolean repairing = newDamage < oldDamage;
        if (!repairing && stack.containsEnchantment(Enchantment.DURABILITY)) {
            int durabilityLevel = (stack.getEnchantmentLevel(Enchantment.DURABILITY) + 1);
            double chanceOfDamage = (double) 100 / (durabilityLevel);

            String name = stack.getType().toString().toLowerCase();
            if (name.contains("_helmet") || name.contains("_chestplate") || name.contains("_leggings") || name.contains("_boots"))
                chanceOfDamage = 60 + ((double) 40 / durabilityLevel);

            int n = rng.nextInt(100) + 1;
            if (n > chanceOfDamage) {
                Log.logInfo(LOG + "Unbreaking prevented damage to " + describe(stack)
                        + " (rolled " + n + ", needed <= " + String.format("%.1f", chanceOfDamage) + ").", Verbosity.HIGHEST);
                return false;
            }
        }

        boolean broke = false;
        if (newDamage >= maxDamage) {
            newDamage = maxDamage;
            broke = true;
        } else if (newDamage < 0) {
            newDamage = 0; // fixtool can't repair past "brand new"
        }

        damageable.setDamage(newDamage);
        stack.setItemMeta(damageable);

        String change = repairing ? "repaired " + (oldDamage - newDamage) : "damaged " + (newDamage - oldDamage);
        Log.logInfo(LOG + change + " on " + describe(stack) + " (durability " + (maxDamage - oldDamage) + " -> "
                + (maxDamage - newDamage) + " of " + maxDamage + ")" + (broke ? "; tool broke." : "."), Verbosity.HIGH);
        return broke;
    }
    private void resetDurability(ItemStack stack) {
        if (stack.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(0);
            stack.setItemMeta(damageable);
        }
    }
    private static String describe(ItemStack stack) {
        if (stack == null) return "nothing";
        return stack.getAmount() + "x " + describeName(stack);
    }
    private static String describeName(ItemStack stack) {
        String name = stack.getType().name();
        ItemMeta meta = stack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) name += " \"" + meta.getDisplayName() + "\"";
        return name;
    }

    public boolean isReplacement() {
        return this.replaceItem != null;
    }
    public boolean isDamage() {
        return this.durabilityRange != null;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        if (durabilityRange != null) sb.append("durability: ").append(durabilityRange).append(", ");
        if (consumeRange != null) sb.append("consume: ").append(consumeRange).append(", ");
        if (replaceItem != null) sb.append("replace: ").append(replaceItem).append(" x").append(replaceItemQuantity).append(", ");
        if (sb.length() > 1) sb.setLength(sb.length() - 2);
        return sb.append("}").toString();
    }
}
