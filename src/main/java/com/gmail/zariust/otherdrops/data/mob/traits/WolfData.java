package com.gmail.zariust.otherdrops.data.mob.traits;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.mob.CreatureData;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

public class WolfData extends CreatureData {
    final Boolean angry; // null = wildcard
    final DyeColor collarColor;

    public WolfData(Boolean angry, DyeColor collarColor) {
        this.angry = angry;
        this.collarColor = collarColor;
    }

    @Override
    public void setOn(Entity mob, Player owner) {
        if (mob instanceof Wolf z) {
            if (angry != null) if (angry) z.setAngry(true);
            if (collarColor != null) z.setCollarColor(collarColor);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof WolfData vd)) return false;
        if (this.angry != null) if (this.angry != vd.angry) return false;
        if (this.collarColor != null) if (this.collarColor != vd.collarColor) return false;

        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Wolf) {
            return new WolfData(((Wolf) entity).isAngry(), ((Wolf) entity).getCollarColor());
        } else {
            Log.logInfo("WolfData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }
    }

    public static CreatureData parseFromString(String state) {
        Boolean angry = null;
        DyeColor collarColor = null;
        for (String sub : CreatureData.keywords(state)) {
            if (sub.equals("angry")) angry = true;
            else if (sub.equals("neutral")) angry = false;
            else for (DyeColor color : DyeColor.values())
                    if (sub.equals(CreatureData.normalize(color.name()))) collarColor = color;
        }
        return new WolfData(angry, collarColor);
    }

    @Override
    public String toString() {
        String val = "";
        if (angry != null) {
            val += "!";
            val += angry ? "ANGRY" : "NEUTRAL";
        }
        if (collarColor != null) {
            val += "!";
            val += collarColor.name();
        }
        return val;
    }

    @Override
    public String get(Enum<?> creature) {
        if (creature instanceof EntityType) return this.toString();
        return "";
    }

}
