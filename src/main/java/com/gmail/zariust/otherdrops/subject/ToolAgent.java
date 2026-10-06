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

package com.gmail.zariust.otherdrops.subject;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.config.ConfigSubject;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.ItemData;
import com.gmail.zariust.otherdrops.data.item.ODItem;
import com.gmail.zariust.otherdrops.options.ConfigOnly;
import com.gmail.zariust.otherdrops.options.ToolDamage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Random;

@ConfigOnly(PlayerSubject.class)
public class ToolAgent implements Agent {
    private final ODItem item;              // config side
    private final ItemStack actualTool;     // player side
    private final Material id;
    private final Data data;                 // player side: damage, for Subject#getData()
    public final int quantityRequired;

    public ToolAgent() {
        this((Material) null);
    }

    public ToolAgent(Material tool) {
        this(tool == null ? null : ODItem.of(tool), 1);
    }

    /** Config side. */
    public ToolAgent(ODItem item, int quantityRequired) {
        this.item = item;
        this.actualTool = null;
        this.id = item == null ? null : item.getMaterial();
        this.data = null;
        this.quantityRequired = quantityRequired;
    }
    /** Player side: the item actually held. */
    public ToolAgent(ItemStack held) {
        this.item = null;
        this.actualTool = held;
        this.id = held == null ? null : held.getType();
        this.data = held == null ? null : new ItemData(held);
        this.quantityRequired = held == null ? 1 : held.getAmount();
    }

    public static Agent parse(ConfigSubject subject) {
        ODItem item = subject.getODItem();
        if (item == null) {
            Log.logInfo("Unrecognized tool: " + subject, Verbosity.HIGHEST);
            return null;
        }
        return new ToolAgent(item, subject.getQuantity());
    }

    public ItemStack getActualTool() {
        return actualTool;
    }

    public ODItem getODItem() {
        return item;
    }

    @Override
    public boolean matches(Subject other) {
        // Only players can hold & use tools
        if (!(other instanceof PlayerSubject player)) return false;
        ToolAgent held = player.getTool();
        if (held == null) return false;
        Material heldType = held.id == null ? Material.AIR : held.id;

        if (item == null) return id == null || id == heldType;
        if (id == Material.AIR) return heldType.isAir();               // empty hand
        if (quantityRequired > held.quantityRequired) {
            Log.logInfo("ToolAgent: holding " + held.quantityRequired + ", need " + quantityRequired + ".", Verbosity.HIGHEST);
            return false;
        }
        return item.matches(held.actualTool);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ToolAgent tool)) return false;
        return Objects.equals(item, tool.item)
                && Objects.equals(id, tool.id)
                && quantityRequired == tool.quantityRequired
                && Objects.equals(actualTool, tool.actualTool);
    }

    @Override
    public int hashCode() {
        return new HashCode(this).get(id);
    }

    public Material getMaterial() {
        return id;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public ItemCategory getType() {
        return ItemCategory.PLAYER;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public void damage(int amount) {
    }

    @Override
    public void damageTool(ToolDamage amount, Random rng) {
    }

    @Override
    public String toString() {
        if (id == null) return "ANY_OBJECT";
        if (item != null) return item + "/" + quantityRequired;
        String damage = data == null ? "" : data.get(id);
        return id + (damage == null || damage.isEmpty() ? "" : "@" + damage) + "/" + quantityRequired;
    }

    @Override
    public String getReadableName() {
        if (id == null) return "ANY_OBJECT";
        return id.toString().toLowerCase().replace("_", " ");
    }
}
