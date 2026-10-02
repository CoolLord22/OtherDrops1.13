package com.gmail.zariust.otherdrops.data.entities;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

public class TameableData extends CreatureData {
    final Boolean isTamed;

    public TameableData(Boolean isTamed) {
        this.isTamed = isTamed;
    }

    @Override
    public void setOn(Entity mob, Player owner) {
        if (mob instanceof Tameable z) {
            if (isTamed != null) if (isTamed) z.setOwner(owner);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof TameableData vd)) return false;
        if (this.isTamed != null) if (this.isTamed != vd.isTamed) return false;
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
        for (String sub : CreatureData.keywords(state)) {
            if (sub.equals("tamed") || sub.equals("tame")) tamed = true;
            else if (sub.equals("untamed") || sub.equals("wild")) tamed = false;
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
        if (creature instanceof EntityType) return this.toString();
        return "";
    }
}
