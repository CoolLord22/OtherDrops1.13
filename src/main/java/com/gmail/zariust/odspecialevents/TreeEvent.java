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

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.event.SimpleDrop;
import com.gmail.zariust.otherdrops.special.SpecialResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;

import java.util.List;

import static com.gmail.zariust.common.Verbosity.HIGH;
import static com.gmail.zariust.common.Verbosity.HIGHEST;

public class TreeEvent extends SpecialResult {
    private final boolean forceTree;
    private TreeType tree = TreeType.TREE;

    public TreeEvent(TreeEvents source, boolean force) {
        super(force ? "FORCETREE" : "TREE", source);
        forceTree = force;
    }

    @Override
    public void executeAt(OccurredEvent event) {
        Location where = event.getLocation().clone();
        if (!where.getBlock().isPassable()) where.add(0, 1, 0);   // a solid block (Ex: one that was clicked): grow on top of it
        Block ground = where.getBlock().getRelative(BlockFace.DOWN);
        Log.logInfo("Event (trees): generating " + tree + " at " + where.getBlockX() + "," + where.getBlockY() + "," + where.getBlockZ()
                + ". Force=" + forceTree + ". Ground: " + ground.getType(), HIGHEST);

        // FORCETREE: briefly turn the ground into dirt so any tree can grow, then put it back.
        // Never on blocks with tile-entity data (chests, spawners, signs…) unless enabled, since that data could be lost.
        BlockState original = ground.getState();
        boolean swapped = forceTree && (!(original instanceof TileState) || TreeEvents.forceOnTileEntities);
        if (swapped) ground.setType(Material.DIRT, false);

        boolean grew = where.getWorld().generateTree(where, tree);
        if (swapped) original.update(true, false);
        if (!grew) Log.logInfo("Event (trees): the " + tree + " couldn't grow there (not enough space, or unsuitable ground).", HIGH);
    }

    @Override
    public void interpretArguments(List<String> args) {
        for (String arg : args) {
            try {
                tree = TreeType.valueOf(arg);
                used(arg);
            } catch (IllegalArgumentException ignored) {
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
