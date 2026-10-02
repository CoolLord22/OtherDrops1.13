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

import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.common.MaterialGroup;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.InventoryHolder;

import java.util.Collections;
import java.util.List;

public class BlockTarget implements Target {
    private final Material id;
    private final Data data;
    private BlockData blockData;
    private Block bl;
    public List<Material> except;
    private String customName;
    private Location location;

    public BlockTarget() {
        this(null, (Data) null);
    }

    public BlockTarget(Material block) {
        this(block, (Data) null); // note: leave as null for "wildcard" to match block with any data
    }

    public BlockTarget(Material block, byte d) {
        this(block, new SimpleData(d));
    }

    public BlockTarget(Material block, Location loc, byte d) {
        this(block == null ? Material.AIR : block, new SimpleData(d));
        location = loc;
    }

    public BlockTarget(Material block, int d) {
        this(block, (byte) d);
    }

    public BlockTarget(Block block) {
        this(block == null ? Material.AIR : block.getType(), getData(block));
        if (block != null) {
            bl = block;
            location = bl.getLocation();
            if (block.getState() instanceof CommandBlock) {
                customName = ((CommandBlock) block.getState()).getName();
            } else if (block.getState() instanceof InventoryHolder) {
                // TODO: This really shouldn't be toString, but rather an inventory view? But not sure how to get that from a break event.
                customName = block.getState().toString();
            }
            blockData = block.getBlockData();
        }
    }

    public BlockTarget(BlockState newState, Location loc) {
        this(newState.getType(), loc, newState.getRawData());
        blockData = newState.getBlockData();
    }

    public BlockTarget(Material mat, Data d) { // The Rome constructor
        id = mat;
        data = d;
    }

    public BlockTarget(FallingBlock what) {
        // TODO: Get the type of falling block rather than assuming it's sand
        this(Material.SAND, 0);
    }

    public BlockTarget(List<Material> except2) {
        this(null, (Data) null);
        except = except2;
    }

    public BlockTarget(Material mat, String customName, int val) {
        this(mat, val);
        this.customName = customName;
    }

    public BlockTarget(Material mat, String customName, Data data) {
        this(mat, data);
        this.customName = customName;
    }

    public BlockTarget(Material mat, String customName) {
        this(mat);
        this.customName = customName;
    }

    @SuppressWarnings("deprecation")
    private static Data getData(Block block) {
        if (block == null) return new SimpleData();
        return switch (block.getType()) {
            case FURNACE, DISPENSER, CHEST -> new ContainerData(block.getState());
            case SPAWNER -> new SpawnerData(block.getState());
            case NOTE_BLOCK -> new NoteData(block.getBlockData());
            case JUKEBOX -> new RecordData(block.getState());
            default -> {
                if (block.getBlockData() instanceof Ageable tempData) {
                    yield new SimpleData(tempData.getAge());
                } else if (block.getBlockData() instanceof Levelled tempData) {
                    yield new SimpleData(tempData.getLevel());
                } else if (block.getBlockData() instanceof Beehive tempData) {
                    yield new SimpleData(tempData.getHoneyLevel());
                }
                yield new SimpleData(block.getData());
            }
        };
    }

    public Material getMaterial() {
        return id;
    }

    @SuppressWarnings("deprecation")
    public int getId() {
        return id.getId();
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BlockTarget targ)) return false;
        return id == targ.id && data.equals(targ.data);
    }

    @Override
    public int hashCode() {
        return new HashCode(this).get(id);
    }

    @Override
    public boolean overrideOn100Percent() {
        return true;
    }

    @Override
    public ItemCategory getType() {
        return ItemCategory.BLOCK;
    }

    @Override
    public boolean matches(Subject block) {
        if (!(block instanceof BlockTarget targ)) return false;
        if (except != null && except.contains(targ.getMaterial())) return false;

        if (this.customName != null && !this.customName.equals(targ.customName)) return false;

        if (data == null) return true;
        if (data instanceof BlockStateData stateData) return stateData.matches(targ.blockData);
        return data.matches(targ.data);
    }

    public static Target parse(String name, String state, String customName) {
        name = name.toUpperCase();
        state = state.toUpperCase();
        if (name.matches("[0-9]+")) {
            Log.logWarning("Error while parsing: " + name + ". Support for numerical IDs has been dropped!");
        }

        // Fetch material
        Material mat = Material.getMaterial(name);
        if (mat == null) mat = CommonMaterial.matchMaterial(name);
        if (mat == null) return null;

        if (!mat.isBlock()) {
            // Only a very select few non-blocks are permitted as a target
            if (mat != Material.PAINTING && mat != Material.MINECART && mat != Material.COMMAND_BLOCK_MINECART && mat != Material.TNT_MINECART && mat != Material.HOPPER_MINECART && mat != Material.FURNACE_MINECART && mat != Material.CHEST_MINECART && mat != Material.OAK_BOAT && mat != Material.ACACIA_BOAT && mat != Material.BIRCH_BOAT && mat != Material.DARK_OAK_BOAT && mat != Material.JUNGLE_BOAT && mat != Material.SPRUCE_BOAT)
                return null;
            else return VehicleTarget.parse(mat, state);
        }

        if (state.isEmpty()) return new BlockTarget(mat, customName);
        if (BlockStateData.isBlockState(state)) {
            try {
                return new BlockTarget(mat, customName, BlockStateData.parse(mat, state));
            } catch (IllegalArgumentException e) {
                Log.logWarning("Invalid block state '" + state + "' for " + mat
                        + " (check the property names and values with the F3 screen or /setblock); skipping...");
                return null;
            }
        }

        // Deprecated: plain numbers (WHEAT@7)
        try {
            int val = Integer.parseInt(state);
            BlockStateData.warnDeprecated(mat, state);
            return new BlockTarget(mat, customName, val);
        } catch (NumberFormatException ignored) {
        }

        // Everything else: legacy named states (deprecated), plus non-state data (containers, spawners, jukeboxes, ranges)
        Data data;
        try {
            data = SimpleData.parse(mat, state);
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (data == null) return new BlockTarget(mat, customName);
        if (data instanceof SimpleData || data instanceof NoteData)
            BlockStateData.warnDeprecated(mat, state);
        return new BlockTarget(mat, customName, data);
    }

    @Override
    public String toString() {
        if (id == null) return except == null ? "ANY_BLOCK" : "ANY_BLOCK_EXCEPT " + except;
        if (blockData != null) {                       // a real block from an event: show its full state
            String state = stateString(blockData);
            return state.isEmpty() ? id.toString() : id + "@" + state;
        }
        if (data == null) return id.toString();
        String dataString = data.get(id);
        return dataString == null || dataString.isEmpty() ? id.toString() : id + "@" + dataString;
    }

    /** "[facing=north,honey_level=3]" from "minecraft:beehive[facing=north,honey_level=3]", or "" if the block has no properties. */
    private static String stateString(BlockData blockData) {
        String full = blockData.getAsString();
        int bracket = full.indexOf('[');
        return bracket < 0 ? "" : full.substring(bracket);
    }

    @Override
    public List<Target> canMatch() {
        if (id == null) return new BlocksTarget(MaterialGroup.ANY_BLOCK).canMatch();
        return Collections.singletonList(this);
    }

    @Override
    public String getKey() {
        return id.toString();
    }

    @Override
    public void setTo(BlockTarget replacement) {
        if (location == null) {
            Log.logInfo("Cannot replace block, location is null.", Verbosity.HIGH);
            return;
        }
        bl = location.getBlock();
        bl.setType(replacement.getMaterial());
        BlockState state = bl.getState();
        if (replacement.data != null) replacement.data.setOn(state);
        state.update(true);
    }

    @Override
    public Location getLocation() {
        if (location != null) return location;
        return null;
    }

    public Block getBlock() {
        return bl;
    }

    @Override
    public String getReadableName() {
        return id.toString().toLowerCase().replaceAll("[-_]", " ");
    }

}
