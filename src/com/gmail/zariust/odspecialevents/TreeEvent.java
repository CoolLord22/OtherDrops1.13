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

package com.gmail.zariust.odspecialevents;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

import static com.gmail.zariust.common.Verbosity.*;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.event.SimpleDrop;
import com.gmail.zariust.otherdrops.special.SpecialResult;

public class TreeEvent extends SpecialResult {
    private boolean               forceTree;
    private TreeType              tree         = TreeType.TREE;
    private static List<Material> tileEntities = Arrays.asList(
    												   Material.BLACK_BANNER, Material.BLACK_WALL_BANNER,
    												   Material.BLUE_BANNER, Material.BLUE_WALL_BANNER, Material.BROWN_BANNER, Material.BROWN_WALL_BANNER,
    												   Material.CYAN_BANNER, Material.CYAN_WALL_BANNER, Material.GRAY_BANNER, Material.GRAY_WALL_BANNER,
    												   Material.GREEN_BANNER, Material.GREEN_WALL_BANNER, Material.LIGHT_BLUE_BANNER, Material.LIGHT_BLUE_WALL_BANNER,
    												   Material.LIGHT_GRAY_BANNER, Material.LIGHT_GRAY_WALL_BANNER, Material.LIME_BANNER, Material.LIME_WALL_BANNER,
    												   Material.MAGENTA_BANNER, Material.MAGENTA_WALL_BANNER, Material.ORANGE_BANNER, Material.ORANGE_WALL_BANNER,
    												   Material.PINK_BANNER, Material.PINK_WALL_BANNER, Material.PURPLE_BANNER, Material.PURPLE_WALL_BANNER,
    												   Material.RED_BANNER, Material.RED_WALL_BANNER, Material.YELLOW_BANNER, Material.YELLOW_WALL_BANNER,
    												   Material.WHITE_BANNER, Material.WHITE_WALL_BANNER, Material.CHEST, Material.TRAPPED_CHEST,
    												   Material.DISPENSER, Material.FURNACE, Material.BREWING_STAND, Material.HOPPER,
    												   Material.DROPPER, Material.SHULKER_BOX, Material.BEACON, Material.SPAWNER,
    												   Material.PISTON_HEAD, Material.MOVING_PISTON, Material.JUKEBOX, Material.ENCHANTING_TABLE,
    												   Material.END_PORTAL, Material.ENDER_CHEST, Material.CREEPER_HEAD, Material.CREEPER_WALL_HEAD,
    												   Material.DRAGON_HEAD, Material.DRAGON_WALL_HEAD, Material.PLAYER_HEAD, Material.PLAYER_WALL_HEAD,
    												   Material.ZOMBIE_HEAD, Material.ZOMBIE_WALL_HEAD, Material.WITHER_SKELETON_SKULL, Material.WITHER_SKELETON_WALL_SKULL,
    												   Material.SKELETON_SKULL, Material.SKELETON_WALL_SKULL, Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK,
    												   Material.REPEATING_COMMAND_BLOCK, Material.END_GATEWAY, Material.STRUCTURE_BLOCK, Material.STRUCTURE_VOID,
    												   Material.DAYLIGHT_DETECTOR, Material.COMPARATOR, Material.CONDUIT
    												   );

    public TreeEvent(TreeEvents source, boolean force) {
        super(force ? "FORCETREE" : "TREE", source);
        forceTree = force;
    }

    @Override
    public void executeAt(OccurredEvent event) {
        Location where = event.getLocation().clone(); // clone, just in case we
                                                      // want to modify the
                                                      // location later
        Log.logInfo(
                "Event (trees): generating tree. Force="
                        + forceTree
                        + ". Block at 'root' location is: "
                        + where.clone().add(0, -1, 0).getBlock().getType()
                                .toString(), HIGHEST);
        Block block = where.getBlock().getRelative(BlockFace.DOWN);
        BlockState state = block.getState();
        if (forceTree
                && (!tileEntities.contains(state.getType()) || TreeEvents.forceOnTileEntities)) {
            block.setType(Material.DIRT);
        }
        // TODO: Is there any reason to allow the use of a BlockChangeDelegate
        // here?
        where.getWorld().generateTree(where, tree);
        if (forceTree)
            state.update(true);
    }

    @Override
    public void interpretArguments(List<String> args) {
        for (String arg : args) {
            try {
                tree = TreeType.valueOf(arg);
                used(arg);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    @Override
    public boolean canRunFor(SimpleDrop drop) {
        return true;
    }

    @Override
    public boolean canRunFor(OccurredEvent drop) {
        return true;
    }

}
