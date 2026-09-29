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

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Weather {
    RAIN(true), SNOW(true), THUNDER(true), CLEAR(false), CLOUD(true), NONE(false), STORM(true) {
        @Override
        public boolean matches(Weather sky) {
            return sky == RAIN || sky == SNOW;
        }
    };
    private final boolean stormy;
    private static final Map<String, Weather> nameLookup = new HashMap<>();

    static {
        for (Weather storm : values())
            nameLookup.put(storm.name(), storm);
    }

    Weather(boolean storm) {
        stormy = storm;
    }

    public static Weather match(Block block, boolean hasStorm, boolean thundering) {
        if (block == null || block.getWorld().getEnvironment() != World.Environment.NORMAL) return NONE;
        if (block.getHumidity() <= 0.0) return hasStorm ? CLOUD : NONE;   // desert, savanna, badlands...
        if (!hasStorm) return CLEAR;
        if (block.getTemperature() < 0.15) return SNOW;                  // vanilla's snow threshold (height-adjusted)
        return thundering ? THUNDER : RAIN;
    }

    public boolean isStormy() {
        return stormy;
    }

    public boolean matches(Weather sky) {
        if (stormy && sky == STORM) return true;
        return this == sky;
    }

    public static Weather parse(String storm) {
        return nameLookup.get(storm.toUpperCase());
    }

    public static Map<Weather, Boolean> parseFrom(ConfigurationNode node, Map<Weather, Boolean> def) {
        List<String> weather = OtherDropsConfig.getMaybeList(node, "weather");
        if (weather.isEmpty()) return def;
        Map<Weather, Boolean> result = new HashMap<>();
        result.put(null, OtherDropsConfig.containsAll(weather));
        for (String name : weather) {
            Weather storm = parse(name);
            if (storm != null) result.put(storm, true);
            else if (name.startsWith("-")) {
                result.put(null, true);
                storm = parse(name.substring(1));
                if (storm == null) {
                    Log.logWarning("Invalid weather " + name + "; skipping...");
                    continue;
                }
                result.put(storm, false);
            } else {
                Log.logWarning("Invalid weather " + name + "; skipping...");
            }
        }
        if (result.isEmpty()) return null;
        return result;
    }
}
