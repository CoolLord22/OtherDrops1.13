package com.gmail.zariust.otherdrops.data.entities;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import org.bukkit.entity.*;

public class RabbitData extends CreatureData {
    Rabbit.Type type  = null; // null = wildcard

    public RabbitData(Rabbit.Type type) {
        this.type = type;
    }

    @Override
    public void setOn(Entity entity, Player owner) {
        if (entity instanceof Rabbit) {
            Rabbit rabbit = (Rabbit) entity;
            if (type != null)
                rabbit.setRabbitType(type);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof RabbitData))
            return false;
        RabbitData vd = (RabbitData) d;

        if (this.type != null)
            if (this.type != vd.type)
                return false;

        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Rabbit) {
            Rabbit rabbit = (Rabbit) entity;
            return new RabbitData(rabbit.getRabbitType());
        } else {
            Log.logInfo("RabbitData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }

    public static CreatureData parseFromString(String state) {
        Rabbit.Type thisType = null;

        if (!state.isEmpty() && !state.equals("0")) {
            String[] split = state.split(OtherDropsConfig.CreatureDataSeparator);

            for (String sub : split) {
                sub = sub.toLowerCase().replaceAll("[\\s-_]", "");
                for (Rabbit.Type type : Rabbit.Type.values()) {
                    if (sub.equals(type.name().toLowerCase().replaceAll("[\\s-_]", "")))
                        thisType = type;
                }
                if (thisType == null)
                    Log.logInfo("RabbitData: type not found (" + sub + ")");
            }
        }

        return new RabbitData(thisType);
    }

    @Override
    public String toString() {
        String val = "";
        if (type != null)
            val += type.toString();
        return val;
    }

    @Override
    public String get(Enum<?> creature) {
        if (creature instanceof EntityType)
            return this.toString();
        return "";
    }
}
