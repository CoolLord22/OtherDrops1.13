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

import java.util.List;
import java.util.Random;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Sheep;

import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.event.SimpleDrop;
import com.gmail.zariust.otherdrops.special.SpecialResult;
import com.gmail.zariust.otherdrops.subject.Agent;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.ToolAgent;

public class DyeEvent extends SpecialResult {
    private DyeColor colour = null;

    public DyeEvent(SheepEvents source) {
        super("DYE", source);
    }

	@Override
    public void executeAt(OccurredEvent event) {
        DyeColor dye = DyeColor.PINK;
        if (colour == null) {
            Agent agent = event.getTool();
            if (agent instanceof PlayerSubject) {
                ToolAgent tool = ((PlayerSubject) agent).getTool();
                if (tool.getMaterial() == Material.INK_SAC)
                    dye = DyeColor.BLACK;
                if (tool.getMaterial() == Material.LAPIS_LAZULI)
                	dye = DyeColor.BLUE;
                if (tool.getMaterial() == Material.COCOA_BEANS)
                	dye = DyeColor.BROWN;
                if (tool.getMaterial() == Material.CYAN_DYE)
                	dye = DyeColor.CYAN;
                if (tool.getMaterial() == Material.GRAY_DYE)
                	dye = DyeColor.GRAY;
                if (tool.getMaterial() == Material.GREEN_DYE)
                	dye = DyeColor.GREEN;
                if (tool.getMaterial() == Material.LIGHT_BLUE_DYE)
                	dye = DyeColor.LIGHT_BLUE;
                if (tool.getMaterial() == Material.LIGHT_GRAY_DYE)
                	dye = DyeColor.LIGHT_GRAY;
                if (tool.getMaterial() == Material.LIME_DYE)
                	dye = DyeColor.LIME;
                if (tool.getMaterial() == Material.MAGENTA_DYE)
                	dye = DyeColor.MAGENTA;
                if (tool.getMaterial() == Material.ORANGE_DYE)
                	dye = DyeColor.ORANGE;
                if (tool.getMaterial() == Material.PINK_DYE)
                	dye = DyeColor.PINK;
                if (tool.getMaterial() == Material.PURPLE_DYE)
                	dye = DyeColor.PURPLE;
                if (tool.getMaterial() == Material.RED_DYE)
                	dye = DyeColor.RED;
                if (tool.getMaterial() == Material.BONE_MEAL)
                	dye = DyeColor.WHITE;
                if (tool.getMaterial() == Material.YELLOW_DYE)
                	dye = DyeColor.YELLOW;
            }
            if (colour == null) {
            	DyeColor[] dyes = DyeColor.values();
            	int rnd = new Random().nextInt(dyes.length);
            	dye = dyes[rnd];
            }
            	
        } else
            dye = colour;
        CreatureSubject target = (CreatureSubject) event.getTarget();
        Sheep sheep = (Sheep) target.getAgent();
        sheep.setColor(dye);
    }

    @Override
    public void interpretArguments(List<String> args) {
        for (String arg : args) {
            try {
                colour = DyeColor.valueOf(arg);
                used(arg);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    @Override
    public boolean canRunFor(SimpleDrop drop) {
        return SheepEvents.canRunFor(drop);
    }

    @Override
    public boolean canRunFor(OccurredEvent drop) {
        return SheepEvents.canRunFor(drop);
    }

}
