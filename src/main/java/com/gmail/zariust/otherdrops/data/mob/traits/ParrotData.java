package com.gmail.zariust.otherdrops.data.mob.traits;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.mob.CreatureData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;

public class ParrotData extends CreatureData {
    final Parrot.Variant variant; // null = wildcard

    public ParrotData(Parrot.Variant variant) {
        this.variant = variant;
    }

    @Override
    public void setOn(Entity entity, Player owner) {
        if (entity instanceof Parrot parrot) {
            if (variant != null) parrot.setVariant(variant);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof ParrotData vd)) return false;
        if (this.variant != null) if (this.variant != vd.variant) return false;
        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Parrot parrot) {
            return new ParrotData(parrot.getVariant());
        } else {
            Log.logInfo("ParrotData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }

    public static CreatureData parseFromString(String state) {
        Parrot.Variant thisType = null;
        for (String sub : CreatureData.keywords(state))
            for (Parrot.Variant type : Parrot.Variant.values())
                if (sub.equals(CreatureData.normalize(type.name()))) thisType = type;
        return new ParrotData(thisType);
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
