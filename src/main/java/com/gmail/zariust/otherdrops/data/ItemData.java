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

import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.gmail.zariust.common.Verbosity.EXTREME;
import static com.gmail.zariust.otherdrops.data.item.properties.DamageProperty.getDurability;

public class ItemData implements Data, RangeableData {
    private int data;
    private String dataString;

    public ItemData(int d) {
        data = d;
    }

    public ItemData(int d, String state) {
        data = d;
        setDataString(state);
    }

    public ItemData(ItemStack item) {
        data = getDurability(item);
    }

    public ItemData(String state) {
        dataString = state; // FIXME: needs more safety checks
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
    public boolean matches(Data d) {
        return data == d.getData();
    }

    @Override
    public String get(Enum<?> mat) {
        if (mat instanceof Material) return get((Material) mat);
        return "";
    }

    @SuppressWarnings("incomplete-switch")
    private String get(Material mat) {
        if (data == -1) return "THIS";
        if (mat.isBlock()) return CommonMaterial.getBlockOrItemData(mat, data);
        switch (mat) {
            case LEATHER_BOOTS:
            case LEATHER_CHESTPLATE:
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
                return dataString;
        }
        if (data > 0) return Integer.toString(data);
        return "";
    }

    @Override
    // Items aren't blocks, so nothing to do here
    public void setOn(BlockState state) {
    }

    @Override
    // Items aren't entities, so nothing to do here
    public void setOn(Entity entity, Player witness) {
    }

    public static Data parse(Material mat, String state) throws IllegalArgumentException {
        if (mat == null || state == null || state.isEmpty()) return null;
        if (state.startsWith("RANGE") || state.matches("[0-9]+-[0-9]+")) return RangeData.parse(state);
        Integer data;
        switch (mat) {
            case POTION:
            case LINGERING_POTION:
            case SPLASH_POTION:
                return parseItemMeta(state, ItemMetaType.POTION);
            case PLAYER_HEAD:
                return parseItemMeta(state, ItemMetaType.SKULL);
            case SPAWNER:
                return SpawnerData.parse(state);
            case LEATHER_BOOTS:
            case LEATHER_CHESTPLATE:
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
                return parseItemMeta(state, ItemMetaType.LEATHER);
            case WRITTEN_BOOK:
                return parseItemMeta(state, ItemMetaType.BOOK);
            case ENCHANTED_BOOK:
                return parseItemMeta(state, ItemMetaType.ENCHANTED_BOOK);
            case FIREWORK_ROCKET:
            case FIREWORK_STAR:
                return parseItemMeta(state, ItemMetaType.FIREWORK);
            default:
                if (mat.isBlock()) {
                    data = CommonMaterial.parseBlockOrItemData(mat, state);
                    break;
                }
                throw new IllegalArgumentException("Illegal data for " + mat + ": " + state);
        }
        if (state.equalsIgnoreCase("THIS")) return new ItemData(-1, state);

        return (data == null) ? null : new ItemData(data, state);
    }

    public enum ItemMetaType {
        LEATHER, SKULL, BOOK, ENCHANTED_BOOK, FIREWORK, POTION
    }

    private static Data parseItemMeta(String state, ItemMetaType metaType) {
        // FIXME: add a safety check here
        Log.logInfo("Parsing for possible metadata: " + state + " type=" + metaType.toString(), Verbosity.HIGH);
        int dataVal = 0;
        return new ItemData(dataVal, state);
    }

    @Override
    public String toString() {
        // TODO: Should probably make sure this is not used, and always use the get method instead
        Log.logWarning("ItemData.toString() was called! Is this right?", EXTREME);
        Log.stackTrace();
        return String.valueOf(data);
    }

    @Override
    public int hashCode() {
        return data;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }

    public String getDataString() {
        return dataString;
    }

    @Override
    public Boolean getSheared() {
        // TODO Auto-generated method stub
        return null;
    }
}
