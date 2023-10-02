package com.gmail.zariust.otherdrops.data.entities;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import org.bukkit.entity.*;

public class SteerableData extends CreatureData {
    Boolean isSaddled = null;

    public SteerableData(Boolean isSaddled) {
        this.isSaddled = isSaddled;
    }

    @Override
    public void setOn(Entity mob, Player owner) {
        if (mob instanceof Steerable) {
            Steerable z = (Steerable) mob;
            if (isSaddled != null)
                if (isSaddled)
                    z.setSaddle(true);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof SteerableData))
            return false;

        SteerableData vd = (SteerableData) d;

        if (this.isSaddled != null)
            if (this.isSaddled != vd.isSaddled)
                return false;

        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Steerable) {
            return new SteerableData(((Steerable) entity).hasSaddle());
        } else {
            Log.logInfo("SteerableData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }

    public static CreatureData parseFromString(String state) {
        Boolean saddled = null;

        if (!state.isEmpty() && !state.equals("0")) {
            String[] split = state.split(OtherDropsConfig.CreatureDataSeparator);

            for (String sub : split) {
                sub = sub.toLowerCase().replaceAll("[\\s-_]", "");
                if (sub.contains("!saddled"))
                    saddled = true;
                else if (sub.contains("!unsaddled"))
                    saddled = false;
            }
        }

        return new SteerableData(saddled);
    }

    @Override
    public String toString() {
        String val = "";
        if (isSaddled != null) {
            val += "!";
            val += isSaddled ? "SADDLED" : "UNSADDLED";
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
