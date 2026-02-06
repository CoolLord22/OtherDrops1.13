package main.java.com.gmail.zariust.otherdrops.data.entities;

import main.java.com.gmail.zariust.common.Verbosity;
import main.java.com.gmail.zariust.otherdrops.Log;
import main.java.com.gmail.zariust.otherdrops.data.CreatureData;
import main.java.com.gmail.zariust.otherdrops.data.Data;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MultipleEntityData extends CreatureData {
    private final HashMap<Class<?>, CreatureData> subData;

    public MultipleEntityData() {
        this.subData = new HashMap<>();
    }

    @Override
    public boolean matches(Data d) {
        if (d instanceof MultipleEntityData inputData) {
            for (Map.Entry<Class<?>, CreatureData> entry : subData.entrySet()) {
                String value = entry.getValue().toString();
                if (value.isEmpty()) value = "*";
                Log.logInfo("Comparing data " + entry.getValue().getClass().getSimpleName() + " " + value + " against " + inputData.getDataFromType(entry.getKey()), Verbosity.HIGHEST);
                if (!entry.getValue().matches(inputData.getDataFromType(entry.getKey()))) return false;
            }
        }
        return true;
    }

    @Override
    public void setOn(Entity mob, Player owner) {
        for (CreatureData data : subData.values()) {
            data.setOn(mob, owner);
        }
    }

    public void add(CreatureData d) {
        subData.put(d.getClass(), d);
    }

    public CreatureData getDataFromType(Class<?> type) {
        return this.subData.get(type);
    }

    @Override
    public String toString() {
        StringBuilder val = new StringBuilder();
        for (CreatureData data : subData.values()) {
            val.append(data.toString());
            val.append("!");
        }
        return val.toString();
    }

    @Override
    public String get(Enum<?> creature) {
        if (creature instanceof EntityType) return this.toString();
        return "";
    }
}
