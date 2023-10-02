package com.gmail.zariust.otherdrops.data.entities;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Fox;

public class FoxData extends CreatureData {
    Fox.Type type  = null; // null = wildcard

    public FoxData(Fox.Type type) {
        this.type = type;
    }

    @Override
    public void setOn(Entity entity, Player owner) {
        if (entity instanceof Fox) {
            Fox fox = (Fox) entity;
            if (type != null)
                fox.setFoxType(type);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof FoxData))
            return false;
        FoxData vd = (FoxData) d;

        if (this.type != null)
            if (this.type != vd.type)
                return false;

        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Fox) {
            Fox fox = (Fox) entity;
            return new FoxData(fox.getFoxType());
        } else {
            Log.logInfo("FoxData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }

    public static CreatureData parseFromString(String state) {
        Fox.Type thisType = null;

        if (!state.isEmpty() && !state.equals("0")) {
            String[] split = state.split(OtherDropsConfig.CreatureDataSeparator);

            for (String sub : split) {
                sub = sub.toLowerCase().replaceAll("[\\s-_]", "");
                for (Fox.Type type : Fox.Type.values()) {
                    if (sub.equals(type.name().toLowerCase().replaceAll("[\\s-_]", "")))
                        thisType = type;
                }
                if (thisType == null)
                    Log.logInfo("FoxData: type not found (" + sub + ")");
            }
        }

        return new FoxData(thisType);
    }

    @Override
    public String toString() {
        String val = "";
        if (type != null)
            val += type.toString();
        return val;
    }

    @Override
    public String get(Enum<?> creature) {
        if (creature instanceof EntityType)
            return this.toString();
        return "";
    }
}
