package com.gmail.zariust.otherdrops.data.entities;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class FrogData extends CreatureData {
    Frog.Variant variant = null; // null = wildcard

    public FrogData(Frog.Variant variant) {
        this.variant = variant;
    }

    @Override
    public void setOn(Entity entity, Player owner) {
        if (entity instanceof Frog) {
            Frog frog = (Frog) entity;
            if (variant != null)
                frog.setVariant(variant);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof FrogData))
            return false;
        FrogData vd = (FrogData) d;

        if (this.variant != null)
            if (this.variant != vd.variant)
                return false;

        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Frog) {
            Frog frog = (Frog) entity;
            return new FrogData(frog.getVariant());
        } else {
            Log.logInfo("FrogData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }

    public static CreatureData parseFromString(String state) {
        Frog.Variant thisType = null;

        if (!state.isEmpty() && !state.equals("0")) {
            String[] split = state.split(OtherDropsConfig.CreatureDataSeparator);

            for (String sub : split) {
                sub = sub.toLowerCase().replaceAll("[\\s-_]", "");
                for (Frog.Variant type : Frog.Variant.values()) {
                    if (sub.equals(type.name().toLowerCase().replaceAll("[\\s-_]", "")))
                        thisType = type;
                }
                if (thisType == null)
                    Log.logInfo("FrogData: type not found (" + sub + ")");
            }
        }

        return new FrogData(thisType);
    }

    @Override
    public String toString() {
        String val = "";
        if (variant != null)
            val += variant.toString();
        return val;
    }

    @Override
    public String get(Enum<?> creature) {
        if (creature instanceof EntityType)
            return this.toString();
        return "";
    }
}
