package com.gmail.zariust.otherdrops.data.entities;

import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;

public class SheepData extends CreatureData {
    Boolean     sheared = null; // null = wildcard
    DyeColor    color   = null;

    @SuppressWarnings("deprecation")
	public SheepData(Boolean sheared, DyeColor color) {
        this.sheared = sheared;
        this.color = color;
        if (color != null)
            data = color.getWoolData();
    }

    @Override
    public void setOn(Entity mob, Player owner) {
        if (mob instanceof Sheep) {
            Sheep z = (Sheep) mob;
            if (sheared != null)
                if (sheared)
                    z.setSheared(true);
            if (color != null)
                z.setColor(color);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof SheepData))
            return false;
        
        SheepData vd = (SheepData) d;

        if (this.sheared != null)
            if (this.sheared != vd.sheared)
                return false;

        if (this.color != null)
            if (this.color != vd.color)
                return false;

        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Sheep) {
            return new SheepData(((Sheep) entity).isSheared(),
                    ((Sheep) entity).getColor());
        } else {
            Log.logInfo("SheepData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }

    public static CreatureData parseFromString(String state) {
        Log.logInfo("SheepData: parsing from string.", Verbosity.HIGHEST);
        Boolean sheared = null;
        DyeColor thisColor = null;

        if (!state.isEmpty() && !state.equals("0")) {
            String[] splitState = state.split(OtherDropsConfig.CreatureDataSeparator);
            
            for (String sub : splitState) {
            	sub = sub.toLowerCase().replaceAll("[\\s-_]", "");

                if (sub.contains("!sheared"))
                	sheared = true;
                else if (sub.contains("!unsheared"))
                	sheared = false;
                else {
                	sheared = null;
                }
                for(DyeColor color : DyeColor.values()) {
                    if (sub.replaceAll("!", "").equals(color.name().toLowerCase().replaceAll("[\\s-_]", "")))
                        thisColor = color;
                }
                if (sheared == null && thisColor == null)
                    Log.logInfo("SheepData: invalid data passed (" + sub + ")");
            }
        }

        return new SheepData(sheared, thisColor);
    }

    @Override
    public String toString() {
        String val = "";
        if (sheared != null) {
            val += "!";
            val += sheared ? "SHEARED" : "UNSHEARED";
        }
        if (color != null) {
            val += "!";
            val += color.toString();
        }
        return val;
    }

    @Override
    public String get(Enum<?> creature) {
        if (creature instanceof EntityType)
            return this.toString();
        return "";
    }

}
