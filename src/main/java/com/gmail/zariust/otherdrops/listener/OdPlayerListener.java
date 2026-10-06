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

package com.gmail.zariust.otherdrops.listener;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.DropCreateException;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class OdPlayerListener implements Listener {
    private final OtherDrops parent;

    public OdPlayerListener(OtherDrops instance) {
        parent = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Right-clicks fire once per hand. With an empty off hand, the main-hand event already covered this click.
        if (isEmptyOffHandEcho(event.getHand(), event.getPlayer())) return;

        // Respect other plugins (protection, claims...). This event has two results instead of one "cancelled":
        //  - clicking a block: skip if another plugin denied using that block
        //  - clicking air: there's no block (so useInteractedBlock() is always DENY); skip only if item use was denied
        Block clicked = event.getClickedBlock();
        if (clicked != null ? event.useInteractedBlock() == Event.Result.DENY : event.useItemInHand() == Event.Result.DENY) {
            Log.logInfo("Interact denied by another plugin - skipping.", Verbosity.HIGHEST);
            return;
        }
        // TODO Make configurable for creative players
        // Clicking air: use the block the player is looking at (or their own block if looking at the sky)
        Block targetBlock = clicked != null ? clicked : event.getPlayer().getTargetBlockExact(200);
        if (targetBlock == null) targetBlock = event.getPlayer().getLocation().getBlock();

        OccurredEvent drop = new OccurredEvent(event, targetBlock);
        parent.sectionManager.performDrop(drop);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (skipEmptyHand(event.getHand(), event.getPlayer())) return;
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            // skip drops for creative mode - TODO: make this configurable?
        } else {
            OccurredEvent drop = new OccurredEvent(event);
            parent.sectionManager.performDrop(drop);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) throws DropCreateException {
        if (!OtherDropsConfig.dropForItemDrop) return;
        event.setCancelled(true);
        OccurredEvent drop = new OccurredEvent(event);
        parent.sectionManager.performDrop(drop);
    }

    /** Right-clicks fire once per hand. When exactly one hand holds something, only that hand counts. */
    private static boolean skipEmptyHand(EquipmentSlot hand, Player player) {
        boolean mainEmpty = player.getInventory().getItemInMainHand().getType().isAir();
        boolean offEmpty = player.getInventory().getItemInOffHand().getType().isAir();
        if (hand == EquipmentSlot.OFF_HAND) return offEmpty;            // empty off hand: the main hand covers it
        if (hand == EquipmentSlot.HAND) return mainEmpty && !offEmpty;  // empty main hand + item in off hand: the off hand covers it
        return false;
    }
    private static boolean isEmptyOffHandEcho(EquipmentSlot hand, Player player) {
        return hand == EquipmentSlot.OFF_HAND && player.getInventory().getItemInOffHand().getType().isAir();
    }
}
