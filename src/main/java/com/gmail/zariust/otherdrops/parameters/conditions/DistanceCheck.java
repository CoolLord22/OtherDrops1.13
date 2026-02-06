package main.java.com.gmail.zariust.otherdrops.parameters.conditions;

import main.java.com.gmail.zariust.common.Verbosity;
import main.java.com.gmail.zariust.otherdrops.ConfigurationNode;
import main.java.com.gmail.zariust.otherdrops.Log;
import main.java.com.gmail.zariust.otherdrops.event.CustomDrop;
import main.java.com.gmail.zariust.otherdrops.event.OccurredEvent;
import main.java.com.gmail.zariust.otherdrops.parameters.Condition;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class DistanceCheck extends Condition {
    private final Location locCheck;
    private final Integer distance;

    public DistanceCheck(Integer distance, Location locCheck) {
        this.distance = distance;
        this.locCheck = locCheck;
    }

    @Override
    public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        if (locCheck == null) return false;
        Location loc = occurrence.getLocation();

        Log.logInfo("DistanceCheck - start", Verbosity.HIGHEST);

        Log.logInfo("DistanceCheck - " + loc.toString() + " vs " + locCheck, Verbosity.HIGH);

        Double actualDistance = check2dDistance(loc.getX(), loc.getZ(), locCheck.getX(), locCheck.getZ());
        return actualDistance > distance;
    }

    private Double check2dDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    @Override
    public List<Condition> parse(ConfigurationNode node) {
        Location locationToMeasureAgainst = new Location(null, 0, 0, 0);
        String getConfig = node.getString("distance");
        Log.logInfo("Loading distance condition: " + getConfig, Verbosity.HIGHEST);
        if (getConfig == null) return null;

        String[] split = getConfig.split("@");
        if (split.length > 1) {
            String[] split2 = split[1].split(";");
            locationToMeasureAgainst = new Location(null, Double.parseDouble(split2[0]), Double.parseDouble(split2[1]), Double.parseDouble(split2[2]));
        }

        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new DistanceCheck(Integer.valueOf(split[0]), locationToMeasureAgainst));
        return conditionList;
    }

}
