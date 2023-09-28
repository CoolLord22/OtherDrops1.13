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

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.event.SimpleDrop;
import com.gmail.zariust.otherdrops.special.SpecialResult;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.List;

public class ThunderEvent extends SpecialResult {
    private int duration = 2400; // default duration = 2 minutes

    public ThunderEvent(WeatherEvents source) {
        super("THUNDERSTORM", source);
    }

    @Override
    public void executeAt(OccurredEvent event) {
        World world = event.getWorld();
        if (duration == 0)
            world.setThundering(false);
        else {
            world.setThundering(true);
            if (duration > 0)
                world.setThunderDuration(duration);
        }
    }

    @Override
    public void interpretArguments(List<String> args) {
        for (String time : args) {
            if (time.equalsIgnoreCase("ON")) {
                duration = -1;
                used(time);
            } else if (time.equalsIgnoreCase("OFF")) {
                duration = 0;
                used(time);
            } else
                try {
                    duration = Short.parseShort(time);
                    used(time);
                } catch (NumberFormatException e) {
                }
        }
    }

    @Override
    public boolean canRunFor(SimpleDrop drop) {
        Biome biome = drop.getTarget().getLocation().getBlock().getBiome();
        return !OtherDrops.NetherBiomes.contains(biome) && biome != Biome.THE_END;
    }

    @Override
    public boolean canRunFor(OccurredEvent drop) {
        Biome biome = drop.getBiome();
        return !OtherDrops.NetherBiomes.contains(biome) && biome != Biome.THE_END;
    }
}
