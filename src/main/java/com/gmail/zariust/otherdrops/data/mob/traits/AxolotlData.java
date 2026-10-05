package com.gmail.zariust.otherdrops.data.mob.traits;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.mob.CreatureData;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class AxolotlData extends CreatureData {
    final Axolotl.Variant variant; // null = wildcard

    public AxolotlData(Axolotl.Variant variant) {
        this.variant = variant;
    }

    @Override
    public void setOn(Entity entity, Player owner) {
        if (entity instanceof Axolotl axolotl) {
            if (variant != null) axolotl.setVariant(variant);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof AxolotlData vd)) return false;
        if (this.variant != null) if (this.variant != vd.variant) return false;
        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Axolotl axolotl) {
            return new AxolotlData(axolotl.getVariant());
        } else {
            Log.logInfo("AxolotlData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }
    }

    public static CreatureData parseFromString(String state) {
        Axolotl.Variant thisType = null;
        for (String sub : CreatureData.keywords(state))
            for (Axolotl.Variant type : Axolotl.Variant.values())
                if (sub.equals(CreatureData.normalize(type.name()))) thisType = type;
        return new AxolotlData(thisType);
    }

    @Override
    public String toString() {
        String val = "";
        if (variant != null) val += variant.toString();
        return val;
    }

    @Override
    public String get(Enum<?> creature) {
        if (creature instanceof EntityType) return this.toString();
        return "";
    }
}
