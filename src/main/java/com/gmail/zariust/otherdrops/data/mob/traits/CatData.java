package com.gmail.zariust.otherdrops.data.mob.traits;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.mob.CreatureData;
import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CatData extends CreatureData {
    final Cat.Type type; // null = wildcard
    final DyeColor collarColor;

    public CatData(Cat.Type type, DyeColor collarColor) {
        this.type = type;
        this.collarColor = collarColor;
    }

    @Override
    public void setOn(Entity entity, Player owner) {
        if (entity instanceof Cat cat) {
            if (type != null) cat.setCatType(type);
            if (collarColor != null) cat.setCollarColor(collarColor);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof CatData vd)) return false;
        if (this.type != null) if (this.type != vd.type) return false;
        if (this.collarColor != null) if (this.collarColor != vd.collarColor) return false;
        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Cat cat) {
            return new CatData(cat.getCatType(), cat.getCollarColor());
        } else {
            Log.logInfo("CatData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }

    public static CreatureData parseFromString(String state) {
        Cat.Type thisType = null;
        DyeColor collarColor = null;
        for (String keyword : CreatureData.keywords(state)) {
            String sub = keyword.replace("cat", "");   // "blackcat" -> "black" (kept for compatibility)
            for (Cat.Type type : Cat.Type.values())
                if (sub.equals(CreatureData.normalize(type.name()))) thisType = type;
            for (DyeColor color : DyeColor.values())
                if (sub.equals(CreatureData.normalize(color.name()))) collarColor = color;
        }
        return new CatData(thisType, collarColor);
    }

    @Override
    public String toString() {
        String val = "";
        if (type != null) val += type.toString();
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
