package main.java.com.gmail.zariust.otherdrops.parameters;

import main.java.com.gmail.zariust.common.Verbosity;
import main.java.com.gmail.zariust.otherdrops.ConfigurationNode;
import main.java.com.gmail.zariust.otherdrops.Log;
import main.java.com.gmail.zariust.otherdrops.event.CustomDrop;
import main.java.com.gmail.zariust.otherdrops.event.OccurredEvent;
import main.java.com.gmail.zariust.otherdrops.parameters.conditions.*;

import java.util.ArrayList;
import java.util.HashSet;
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

    protected static final Set<Condition> conditions = new HashSet<>();

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
        registerCondition(new AttackRangeCheck(null));
        registerCondition(new BiomeCheck(null));
        registerCondition(new BlockFaceCheck(null));
        registerCondition(new BlockPlaceByCheck(null));
        registerCondition(new CooldownCheck(null, null, null));
        registerCondition(new DistanceCheck(null, null));
        registerCondition(new FishhookBiomeCheck(null));
        registerCondition(new HeightCheck(null));
        registerCondition(new ItemRequirementCheck(null));
        registerCondition(new JobNameCheck(null, null));
        registerCondition(new LightLevelCheck(null));
        registerCondition(new LoreNameCheck(null));
        registerCondition(new LoreLineCheck(null));
        registerCondition(new MobSpawnerCheck(null, null));
        registerCondition(new MoonPhaseCheck(null));
        registerCondition(new PermissionCheck(null));
        registerCondition(new PermissionGroupCheck(null));
        registerCondition(new PlayerSneakCheck(null));
        registerCondition(new PotionEffectCondition(null));
        registerCondition(new RegionCheck(null));
        registerCondition(new SpawnedCheck(null));
        registerCondition(new TimeCheck(null));
        registerCondition(new WeatherCheck(null));
        registerCondition(new WorldCheck(null));
    }
}
