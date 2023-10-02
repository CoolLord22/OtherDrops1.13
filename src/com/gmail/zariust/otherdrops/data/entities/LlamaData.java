package com.gmail.zariust.otherdrops.data.entities;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class LlamaData extends CreatureData {
    Llama.Color variant = null; // null = wildcard

    public LlamaData(Llama.Color variant) {
        this.variant = variant;
    }

    @Override
    public void setOn(Entity entity, Player owner) {
        if (entity instanceof Llama) {
            Llama llama = (Llama) entity;
            if (variant != null)
                llama.setColor(variant);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof LlamaData))
            return false;
        LlamaData vd = (LlamaData) d;

        if (this.variant != null)
            if (this.variant != vd.variant)
                return false;

        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Llama) {
            Llama llama = (Llama) entity;
            return new LlamaData(llama.getColor());
        } else {
            Log.logInfo("LlamaData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }

    public static CreatureData parseFromString(String state) {
        Llama.Color thisType = null;

        if (!state.isEmpty() && !state.equals("0")) {
            String[] split = state.split(OtherDropsConfig.CreatureDataSeparator);

            for (String sub : split) {
                sub = sub.toLowerCase().replaceAll("[\\s-_]", "");
                for (Llama.Color type : Llama.Color.values()) {
                    if (sub.equals(type.name().toLowerCase().replaceAll("[\\s-_]", "")))
                        thisType = type;
                }
                if (thisType == null)
                    Log.logInfo("LlamaData: type not found (" + sub + ")");
            }
        }

        return new LlamaData(thisType);
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
