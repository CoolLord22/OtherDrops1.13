package com.gmail.zariust.otherdrops.data.entities;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import org.bukkit.DyeColor;
import org.bukkit.entity.*;

public class CatData extends CreatureData {
    Cat.Type type  = null; // null = wildcard
    DyeColor collarColor = null;

    public CatData(Cat.Type type, DyeColor collarColor) {
        this.type = type;
        this.collarColor = collarColor;
    }

    @Override
    public void setOn(Entity entity, Player owner) {
        if (entity instanceof Cat) {
            Cat cat = (Cat) entity;
            if (type != null)
                cat.setCatType(type);

            if (collarColor != null)
                cat.setCollarColor(collarColor);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof CatData))
            return false;
        CatData vd = (CatData) d;

        if (this.type != null)
            if (this.type != vd.type)
                return false;

        if (this.collarColor != null)
            if (this.collarColor != vd.collarColor)
                return false;

        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Cat) {
            Cat cat = (Cat) entity;
            return new CatData(cat.getCatType(), cat.getCollarColor());
        } else {
            Log.logInfo("CatData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }

    public static CreatureData parseFromString(String state) {
        Cat.Type thisType = null;
        DyeColor collarColor = null;

        if (!state.isEmpty() && !state.equals("0")) {
            String[] split = state.split(OtherDropsConfig.CreatureDataSeparator);

            for (String sub : split) {
                sub = sub.toLowerCase().replaceAll("[\\s-_]", "").replaceAll("cat", "");
                for (Cat.Type type : Cat.Type.values()) {
                    if (sub.equals(type.name().toLowerCase().replaceAll("[\\s-_]", "")))
                        thisType = type;
                }
                try {
                    collarColor = DyeColor.valueOf(sub.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // do nothing
                }
                if (thisType == null && collarColor == null)
                    Log.logInfo("CatData: type not found (" + sub + ")");
            }
        }

        return new CatData(thisType, collarColor);
    }

    @Override
    public String toString() {
        String val = "";
        if (type != null)
            val += type.toString();
        if (collarColor != null) {
            val += "!";
            val += collarColor.name();
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
