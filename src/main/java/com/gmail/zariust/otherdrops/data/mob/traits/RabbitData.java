package com.gmail.zariust.otherdrops.data.mob.traits;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.mob.CreatureData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;

public class RabbitData extends CreatureData {
    final Rabbit.Type type; // null = wildcard

    public RabbitData(Rabbit.Type type) {
        this.type = type;
    }

    @Override
    public void setOn(Entity entity, Player owner) {
        if (entity instanceof Rabbit rabbit) {
            if (type != null) rabbit.setRabbitType(type);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof RabbitData vd)) return false;
        if (this.type != null) if (this.type != vd.type) return false;
        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Rabbit rabbit) {
            return new RabbitData(rabbit.getRabbitType());
        } else {
            Log.logInfo("RabbitData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }
    }

    public static CreatureData parseFromString(String state) {
        Rabbit.Type thisType = null;
        for (String sub : CreatureData.keywords(state))
            for (Rabbit.Type type : Rabbit.Type.values())
                if (sub.equals(CreatureData.normalize(type.name()))) thisType = type;
        return new RabbitData(thisType);
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
