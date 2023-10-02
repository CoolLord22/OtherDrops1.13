package com.gmail.zariust.otherdrops.data.entities;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ParrotData extends CreatureData {
    Parrot.Variant variant = null; // null = wildcard

    public ParrotData(Parrot.Variant variant) {
        this.variant = variant;
    }

    @Override
    public void setOn(Entity entity, Player owner) {
        if (entity instanceof Parrot) {
            Parrot parrot = (Parrot) entity;
            if (variant != null)
                parrot.setVariant(variant);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof ParrotData))
            return false;
        ParrotData vd = (ParrotData) d;

        if (this.variant != null)
            if (this.variant != vd.variant)
                return false;

        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Parrot) {
            Parrot parrot = (Parrot) entity;
            return new ParrotData(parrot.getVariant());
        } else {
            Log.logInfo("ParrotData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }

    public static CreatureData parseFromString(String state) {
        Parrot.Variant thisType = null;

        if (!state.isEmpty() && !state.equals("0")) {
            String[] split = state.split(OtherDropsConfig.CreatureDataSeparator);

            for (String sub : split) {
                sub = sub.toLowerCase().replaceAll("[\\s-_]", "");
                for (Parrot.Variant type : Parrot.Variant.values()) {
                    if (sub.equals(type.name().toLowerCase().replaceAll("[\\s-_]", "")))
                        thisType = type;
                }
                if (thisType == null)
                    Log.logInfo("ParrotData: type not found (" + sub + ")");
            }
        }

        return new ParrotData(thisType);
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
