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

package main.java.com.gmail.zariust.odspecialevents;

import main.java.com.gmail.zariust.common.Verbosity;
import main.java.com.gmail.zariust.otherdrops.ConfigurationNode;
import main.java.com.gmail.zariust.otherdrops.special.SpecialResult;
import main.java.com.gmail.zariust.otherdrops.special.SpecialResultHandler;

import java.util.Arrays;
import java.util.List;

public class TreeEvents extends SpecialResultHandler {
    public static boolean forceOnTileEntities;

    @Override
    public SpecialResult getNewEvent(String name) {
        if (name.equalsIgnoreCase("TREE")) return new TreeEvent(this, false);
        else if (name.equalsIgnoreCase("FORCETREE")) return new TreeEvent(this, true);
        return null;
    }

    @Override
    public void onLoad() {
        ConfigurationNode configNode = getConfiguration();
        forceOnTileEntities = configNode != null && configNode.getBoolean("force-tile-entities", false);
        logInfo("Trees v" + getVersion() + " loaded.", Verbosity.HIGH);
    }

    @Override
    public List<String> getEvents() {
        return Arrays.asList("TREE", "FORCETREE");
    }

    @Override
    public String getName() {
        return "main/resources/Trees";
    }

}
