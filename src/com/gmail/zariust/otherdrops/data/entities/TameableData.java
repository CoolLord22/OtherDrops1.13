package com.gmail.zariust.otherdrops.data.entities;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import org.bukkit.entity.*;

public class TameableData extends CreatureData {
    Boolean isTamed = null;

    public TameableData(Boolean isTamed) {
        this.isTamed = isTamed;
    }

    @Override
    public void setOn(Entity mob, Player owner) {
        if (mob instanceof Tameable) {
            Tameable z = (Tameable) mob;
            if (isTamed != null)
                if (isTamed)
                    z.setOwner(owner);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof TameableData))
            return false;

        TameableData vd = (TameableData) d;

        if (this.isTamed != null)
            if (this.isTamed != vd.isTamed)
                return false;

        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Tameable) {
            return new TameableData(((Tameable) entity).isTamed());
        } else {
            Log.logInfo("TameableData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }

    public static CreatureData parseFromString(String state) {
        Boolean tamed = null;

        if (!state.isEmpty() && !state.equals("0")) {
            String[] split = state.split(OtherDropsConfig.CreatureDataSeparator);

            for (String sub : split) {
                sub = sub.toLowerCase().replaceAll("[\\s-_]", "");
                if (sub.matches("(tame[d]*)"))
                    tamed = true;
                else if (sub.matches("(untamed|wild)"))
                    tamed = false;
            }
        }

        return new TameableData(tamed);
    }

    @Override
    public String toString() {
        String val = "";
        if (isTamed != null) {
            val += "!";
            val += isTamed ? "TAME" : "UNTAMED";
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
