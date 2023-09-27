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

import com.gmail.zariust.common.CommonEnchantments;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.things.ODItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class ToolDamage {
    private ShortRange durabilityRange;
    private IntRange   consumeRange;
    private ODItem     replaceItem;
    private IntRange   replaceItemQuantity = new IntRange(1);

    public ToolDamage() {
        this(null, 1);
    }

    public ToolDamage(Integer damage) {
        this(damage, 1);
    }

    public ToolDamage(Integer damage, int replaceQuantity) {
        if (damage != null)
            durabilityRange = ShortRange.parse(String.valueOf(damage));
        this.replaceItemQuantity = new IntRange(replaceQuantity);
    }

	public boolean apply(ItemStack stack, Random rng) {
        boolean fullyConsumed = false;
        short maxDurability = stack.getType().getMaxDurability();
        if (maxDurability > 0 && durabilityRange != null) {
            short durability = stack.getDurability();
            short damage = durabilityRange.getRandomIn(rng);
            
            if (durability + damage >= maxDurability)
                fullyConsumed = true;
            setDurability(stack, (short) (durability + damage), rng);
        }
        if (consumeRange != null && (fullyConsumed || durabilityRange == null)) {
            if (fullyConsumed) {
                fullyConsumed = false;
                setDurability(stack, (short) 0, rng);
            }
            int count = stack.getAmount();
            int take = consumeRange.getRandomIn(rng);
            if (count <= take)
                fullyConsumed = true;
            else
                stack.setAmount(count - take);
            Log.logInfo("Tool consume: " + take + "x " + stack
                    + " consumed (" + (count - take) + ") remaining.",
                    Verbosity.HIGH);
        }
        if (replaceItem != null && fullyConsumed) {
            fullyConsumed = false;
            setDurability(stack, (short) 0, rng);

            stack.setType(replaceItem.getMaterial());
            stack.setAmount(replaceItemQuantity.getRandomIn(OtherDrops.rng));
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(replaceItem.getDisplayName());
            meta.setLore(replaceItem.lore);
            stack.setItemMeta(meta);
            stack = CommonEnchantments.applyEnchantments(stack, replaceItem.getEnchantments());

            Log.logInfo("Tool replaced.", Verbosity.HIGH);
        } else if (durabilityRange == null && consumeRange == null) {
            fullyConsumed = false;
            setDurability(stack, (short) 0, rng);

            stack.setType(replaceItem.getMaterial());
            stack.setAmount(replaceItemQuantity.getRandomIn(OtherDrops.rng));
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(replaceItem.getDisplayName());
            meta.setLore(replaceItem.lore);
            stack.setItemMeta(meta);
            stack = CommonEnchantments.applyEnchantments(stack, replaceItem.getEnchantments());

            Log.logInfo("Tool replaced.", Verbosity.HIGH);
        }
        return fullyConsumed;
    }
	
	private void setDurability(ItemStack stack, short durability, Random rng) {
		if(stack.getItemMeta().isUnbreakable())
			return;
		
		if (stack.containsEnchantment(Enchantment.DURABILITY)) {
        	int durabilityLevel = (stack.getEnchantmentLevel(Enchantment.DURABILITY) + 1);
        	double chanceOfDamage = 100 / (durabilityLevel);
        	
        	String name = stack.getType().toString().toLowerCase();
        	if(name.contains("_helmet") || name.contains("_chestplate") || name.contains("_leggings") || name.contains("_boots"))
        		chanceOfDamage = 60 + (40 / durabilityLevel);

        	int n = rng.nextInt(100) + 1;
        	
        	if(n > chanceOfDamage) {
                Log.logInfo("Tool with unbreaking failed damage (expected behavior).", Verbosity.HIGH);
                return;
        	}
        }

		Log.logInfo("Tool damaged.", Verbosity.HIGH);
        stack.setDurability(durability);
	}

    public static ToolDamage parseFrom(ConfigurationNode node) {
        ToolDamage damage = new ToolDamage();
        // Durability
        String durability = node.getString("damagetool");
        if (durability != null)
            damage.durabilityRange = ShortRange.parse(durability);
        else {
            durability = node.getString("fixtool");
            if (durability != null) {
                ShortRange range = ShortRange.parse(durability);
                damage.durabilityRange = range.negate(range);
            }
        }
        // Amount
        String consume = node.getString("consumetool");
        if (consume != null)
            damage.consumeRange = IntRange.parse(consume);
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
            if (replaceSplit.length > 1)
                damage.replaceItemQuantity = IntRange.parse(replaceSplit[1]);
            damage.replaceItem = ODItem.parseItem(replace.replaceAll("(\\/q#\\d{1,9}-\\d{1,9}|\\/q#\\d{1,9})", ""));

            Log.logInfo("...tool will be replaced by " + damage.replaceItem, Verbosity.NORMAL);
        }
        if (damage.durabilityRange != null || damage.consumeRange != null || damage.replaceItem != null)
            return damage;
        return null;
    }

    public boolean isReplacement() {
        return this.replaceItem != null;
    }

    @Override
    public String toString() {
        StringBuilder dmg = new StringBuilder("{");
        dmg.append("damage: ");
        dmg.append(durabilityRange);
        dmg.append("quantity: ");
        dmg.append(consumeRange);
        dmg.append("replace: ");
        dmg.append(replaceItem);
        dmg.append("}");
        return dmg.toString();
    }
}
