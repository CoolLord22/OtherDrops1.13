package com.gmail.zariust.otherdrops.data.mob.traits;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.mob.CreatureData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;

public class FoxData extends CreatureData {
    final Fox.Type type; // null = wildcard

    public FoxData(Fox.Type type) {
        this.type = type;
    }

    @Override
    public void setOn(Entity entity, Player owner) {
        if (entity instanceof Fox fox) {
            if (type != null) fox.setFoxType(type);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof FoxData vd)) return false;
        if (this.type != null) if (this.type != vd.type) return false;
        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Fox fox) {
            return new FoxData(fox.getFoxType());
        } else {
            Log.logInfo("FoxData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }
    
    public static CreatureData parseFromString(String state) {
        Fox.Type thisType = null;
        for (String sub : CreatureData.keywords(state))
            for (Fox.Type type : Fox.Type.values())
                if (sub.equals(CreatureData.normalize(type.name()))) thisType = type;
        return new FoxData(thisType);
    }

    @Override
    public String toString() {
        String val = "";
        if (type != null) val += type.toString();
        return val;
    }

    @Override
    public String get(Enum<?> creature) {
        if (creature instanceof EntityType) return this.toString();
        return "";
    }
}
