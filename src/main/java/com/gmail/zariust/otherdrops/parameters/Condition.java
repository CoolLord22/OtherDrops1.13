package com.gmail.zariust.otherdrops.parameters;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.conditions.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class Condition extends Parameter {
    String conditionName = "undefined";

    public final boolean check(CustomDrop drop, OccurredEvent occurrence) {
        boolean result = checkInstance(drop, occurrence);
        Log.logInfo("Condition '" + this.getClass().getSimpleName() + "' " + (result ? "passed" : "failed"), Verbosity.HIGHEST);
        return result;
    }

    protected abstract boolean checkInstance(CustomDrop drop, OccurredEvent occurrence);

    // protected abstract static List<Condition> parseInstance(ConfigurationNode node);

    protected static final Set<Condition> conditions = new LinkedHashSet<>();

    // NOTE: currently this function is called before verbosity is loaded from the config file (so debug messages based on Verbosity.HIGH etc. won't work)
    public static boolean registerCondition(Condition register) {
        if (register == null) Log.logInfo("Condition - registering FAILED");
        else conditions.add(register);
        return false;
    }

    public static List<Condition> parseNodes(ConfigurationNode node) {
        List<Condition> conditionsReturn = new ArrayList<>();
        List<Condition> conditionsFromParse;
        for (Condition condition : conditions) {
            conditionsFromParse = condition.parse(node);
            if (conditionsFromParse != null) conditionsReturn.addAll(conditionsFromParse);
        }
        return conditionsReturn;
    }

    abstract public List<Condition> parse(ConfigurationNode parseMe);

    public static void registerDefaultConditions() {
        conditions.clear(); // guard against double registration

        // 1. world / region
        registerCondition(new WorldCheck(null));
        registerCondition(new RegionCheck(null));
        // 2. biome
        registerCondition(new BiomeCheck(null));
        registerCondition(new FishhookBiomeCheck(null));
        // 3. position
        registerCondition(new HeightCheck(null));
        registerCondition(new DistanceCheck(null, null));
        registerCondition(new BlockFaceCheck(null));
        // 4. time & environment
        registerCondition(new TimeCheck(null));
        registerCondition(new MoonPhaseCheck(null));
        registerCondition(new WeatherCheck(null));
        // 5. target origin
        registerCondition(new BlockPlaceByCheck(null));
        registerCondition(new SpawnedCheck(null));
        // 6. light / range
        registerCondition(new LightLevelCheck(null));
        registerCondition(new AttackRangeCheck(null));
        // 7. player state
        registerCondition(new PlayerSneakCheck(null));
        registerCondition(new PermissionCheck(null));
        registerCondition(new PermissionGroupCheck(null));
        registerCondition(new JobNameCheck(null, null));
        registerCondition(new PotionEffectCondition(null));
        // 8. tool details
        registerCondition(new LoreNameCheck(null));
        registerCondition(new LoreLineCheck(null));
        // 9. expensive: scans a cube of blocks
        registerCondition(new MobSpawnerCheck(null, null));
        // 10-11. Committable (side effects applied only when the section actually runs)
        registerCondition(new CooldownCheck(null, null, null));
        registerCondition(new ItemRequirementCheck(null));
    }
}
