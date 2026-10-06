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

package com.gmail.zariust.otherdrops.drop;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.config.ConfigSubject;
import com.gmail.zariust.otherdrops.data.item.ODItem;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemDrop extends DropType {
    private final ODItem odItem;
    private final IntRange quantity;
    private int rolledQuantity;

    public ItemDrop(ODItem odItem) {
        this(odItem, new IntRange(1), 100);
    }

    public ItemDrop(Material material, IntRange amount, double percent) {
        this(ODItem.of(material), amount, percent);
    }

    public ItemDrop(ODItem odItem, IntRange amount, double percent) {
        super(DropCategory.ITEM, percent);
        this.odItem = odItem;
        this.quantity = amount;
    }

    public static DropType parse(String drop, String defaultData, IntRange amount, double chance) {
        ODItem item = ConfigSubject.parseSubject(drop).withDefaultData(defaultData).getODItem();
        return item == null ? null : new ItemDrop(item, amount, chance);
    }

    public ItemStack getItem() {
        return getItem(null, null);
    }

    public ItemStack getItem(Target source, DropFlags flags) {
        rolledQuantity = quantity.getRandomIn(OtherDrops.rng);
        return odItem.create(rolledQuantity, source, flags);
    }

    @Override
    protected DropResult performDrop(Target source, Location where, DropFlags flags) {
        DropResult dropResult = DropResult.getFromOverrideDefault(this.overrideDefault);
        if (odItem == null || quantity.getMax() == 0) return dropResult; // DEFAULT: nothing to drop here
        if (odItem.getMaterial() == Material.AIR) dropResult.setOverrideDefault(true); // NOTHING: always replace vanilla

        ItemStack stack = getItem(source, flags);
        int count = 1; // dropspread off: one (multi-item) stack
        if (flags.spread) { // dropspread on: one item at a time
            stack.setAmount(1);
            count = rolledQuantity;
        }
        Player playerReceivingItem = flags.recipient;
        while (count-- > 0) {
            if ((!OtherDropsConfig.globalFallToGround || flags.dropToInventory) && playerReceivingItem != null) {
                dropResult.addWithoutOverride(drop(playerReceivingItem, stack, where, flags));
            } else {
                dropResult.addWithoutOverride(drop(where, stack, flags));
            }
        }
        return dropResult;
    }

    /** null for DEFAULT, AIR for NOTHING (used by SimpleDrop), otherwise the item's material. */
    public Material getMaterial() {
        return odItem == null ? null : odItem.getMaterial();
    }

    public ODItem getODItem() {
        return odItem;
    }

    /** %d in messages: the item as written, or DEFAULT. */
    @Override
    public String getName() {
        return odItem == null ? "DEFAULT" : odItem.toString();
    }

    @Override
    public double getAmount() {
        return rolledQuantity;
    }

    @Override
    public DoubleRange getAmountRange() {
        return quantity.toDoubleRange();
    }
}
