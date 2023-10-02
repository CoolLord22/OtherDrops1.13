package com.gmail.zariust.otherdrops.data.entities;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import org.bukkit.entity.*;

public class AxolotlData extends CreatureData {
    Axolotl.Variant variant = null; // null = wildcard

    public AxolotlData(Axolotl.Variant variant) {
        this.variant = variant;
    }

    @Override
    public void setOn(Entity entity, Player owner) {
        if (entity instanceof Axolotl) {
            Axolotl axolotl = (Axolotl) entity;
            if (variant != null)
                axolotl.setVariant(variant);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof AxolotlData))
            return false;
        AxolotlData vd = (AxolotlData) d;

        if (this.variant != null)
            if (this.variant != vd.variant)
                return false;

        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Axolotl) {
            Axolotl axolotl = (Axolotl) entity;
            return new AxolotlData(axolotl.getVariant());
        } else {
            Log.logInfo("AxolotlData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }

    public static CreatureData parseFromString(String state) {
        Axolotl.Variant thisType = null;

        if (!state.isEmpty() && !state.equals("0")) {
            String[] split = state.split(OtherDropsConfig.CreatureDataSeparator);

            for (String sub : split) {
                sub = sub.toLowerCase().replaceAll("[\\s-_]", "");
                for (Axolotl.Variant type : Axolotl.Variant.values()) {
                    if (sub.equals(type.name().toLowerCase().replaceAll("[\\s-_]", "")))
                        thisType = type;
                }
                if (thisType == null)
                    Log.logInfo("AxolotlData: type not found (" + sub + ")");
            }
        }

        return new AxolotlData(thisType);
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
