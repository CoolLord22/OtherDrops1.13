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

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import static com.gmail.zariust.common.Verbosity.*;

public class OdEntityListener implements Listener {
    private final OtherDrops parent;

    public OdEntityListener(OtherDrops instance) {
        parent = instance;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Log.logInfo("OnEntityDamage (victim: " + event.getEntity() + ")", EXTREME);
        OccurredEvent drop = new OccurredEvent(event, "hit");
        parent.sectionManager.performDrop(drop);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        // TODO: use get getLastDamageCause rather than checking on each getdamage?
        Log.logInfo("*** OnEntityDeath, before checks (victim: " + event.getEntity() + ")", HIGHEST);
        Entity entity = event.getEntity();

        // If there's no damage record, ignore
        if (entity.getLastDamageCause() == null) {
            Log.logWarning("OnEntityDeath: entity " + entity + " has no 'lastDamageCause'.", HIGH);
            return;
        }

        OccurredEvent drop = new OccurredEvent(event);
        Log.logInfo("EntityDeath drop occurance created. (" + drop + ")", HIGHEST);
        parent.sectionManager.performDrop(drop);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        // TODO: Why was this commented out?
        if (!parent.config.customDropsForExplosions) return;

        // TODO: add a config item to enable enderdragon explosions if people want to use it with v.low chance drops
        if (event.getEntity() instanceof EnderDragon) return; // Enderdragon explosion drops will lag out the server....

        Log.logInfo("Processing explosion...", HIGHEST);
        parent.sectionManager.performDrop(new OccurredEvent(event, event.getEntity()));
    }
}
