package com.gmail.zariust.otherdrops.data.mob.traits;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.mob.CreatureData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Steerable;

public class SteerableData extends CreatureData {
    final Boolean isSaddled;

    public SteerableData(Boolean isSaddled) {
        this.isSaddled = isSaddled;
    }

    @Override
    public void setOn(Entity mob, Player owner) {
        if (mob instanceof Steerable z) {
            if (isSaddled != null) if (isSaddled) z.setSaddle(true);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof SteerableData vd)) return false;
        if (this.isSaddled != null) if (this.isSaddled != vd.isSaddled) return false;
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
        for (String sub : CreatureData.keywords(state)) {
            if (sub.equals("saddled")) saddled = true;
            else if (sub.equals("unsaddled")) saddled = false;
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
        if (creature instanceof EntityType) return this.toString();
        return "";
    }
}
