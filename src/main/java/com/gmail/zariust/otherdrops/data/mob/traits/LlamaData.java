package com.gmail.zariust.otherdrops.data.mob.traits;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.mob.CreatureData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;

public class LlamaData extends CreatureData {
    final Llama.Color variant; // null = wildcard

    public LlamaData(Llama.Color variant) {
        this.variant = variant;
    }

    @Override
    public void setOn(Entity entity, Player owner) {
        if (entity instanceof Llama llama) {
            if (variant != null) llama.setColor(variant);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof LlamaData vd)) return false;
        if (this.variant != null) if (this.variant != vd.variant) return false;
        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Llama llama) {
            return new LlamaData(llama.getColor());
        } else {
            Log.logInfo("LlamaData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }
    }

    public static CreatureData parseFromString(String state) {
        Llama.Color thisType = null;
        for (String sub : CreatureData.keywords(state))
            for (Llama.Color type : Llama.Color.values())
                if (sub.equals(CreatureData.normalize(type.name()))) thisType = type;
        return new LlamaData(thisType);
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
