package main.java.com.gmail.zariust.otherdrops.parameters.conditions;

import main.java.com.gmail.zariust.common.Verbosity;
import main.java.com.gmail.zariust.otherdrops.ConfigurationNode;
import main.java.com.gmail.zariust.otherdrops.Log;
import main.java.com.gmail.zariust.otherdrops.OtherDropsConfig;
import main.java.com.gmail.zariust.otherdrops.event.CustomDrop;
import main.java.com.gmail.zariust.otherdrops.event.OccurredEvent;
import main.java.com.gmail.zariust.otherdrops.parameters.Condition;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnedCheck extends Condition {

    String name = "SpawnedCheck";
    private final Map<String, Boolean> spawnReasonsStored;

    public SpawnedCheck(Map<String, Boolean> value) {
        this.spawnReasonsStored = value;
    }

    @Override
    public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        Entity entity = occurrence.getVictim();
        Log.logInfo("SpawnedCheck - start", Verbosity.HIGHEST);

        if (entity != null) {
            String spawnReason = "";
            if (!entity.getMetadata("CreatureSpawnedBy").isEmpty()) spawnReason = (String) entity.getMetadata("CreatureSpawnedBy").get(0).value();
            Log.logInfo("SpawnedCheck - checking: " + spawnReasonsStored.toString() + " vs actual: " + spawnReason, Verbosity.HIGHEST);
            return CustomDrop.checkList(spawnReason.toUpperCase(), spawnReasonsStored);
        } else {
            Log.logInfo("SpawnCheck - failed, entity = null.", Verbosity.HIGHEST);
            return false;
        }
    }

    @Override
    public List<Condition> parse(ConfigurationNode node) {
        Map<String, Boolean> value;
        value = parseSpawnedFrom(node, null);
        if (value == null) return null;
        OtherDropsConfig.dropForSpawned = true;

        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new SpawnedCheck(value));
        return conditionList;
    }

    public static Map<String, Boolean> parseSpawnedFrom(ConfigurationNode node, Map<String, Boolean> def) {
        List<String> spawnReasons = OtherDropsConfig.getMaybeList(node, "spawnedby");
        if (spawnReasons.isEmpty()) return def;
        HashMap<String, Boolean> result = new HashMap<>();
        result.put(null, OtherDropsConfig.containsAll(spawnReasons));
        for (String name : spawnReasons) {
            name = name.toUpperCase();
            if (name.startsWith("-")) {
                result.put(null, true);
                result.put(name.substring(1), false);
            } else {
                result.put(name, true);
            }
        }
        return result;
    }

}
