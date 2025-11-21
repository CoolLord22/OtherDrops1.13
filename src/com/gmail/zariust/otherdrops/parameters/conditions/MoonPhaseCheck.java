package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MoonPhaseCheck extends Condition {
    public enum MoonPhase {
        FULL_MOON, WANING_GIBBOUS, LAST_QUARTER, WANING_CRESCENT, NEW_MOON, WAXING_CRESCENT, FIRST_QUARTER, WAXING_GIBBOUS;

        public static MoonPhase fromInt(int i) {
            return switch (i) {
                case 0 -> FULL_MOON;
                case 1 -> WANING_GIBBOUS;
                case 2 -> LAST_QUARTER;
                case 3 -> WANING_CRESCENT;
                case 4 -> NEW_MOON;
                case 5 -> WAXING_CRESCENT;
                case 6 -> FIRST_QUARTER;
                case 7 -> WAXING_GIBBOUS;
                default -> throw new IllegalArgumentException("Invalid moon phase index: " + i);
            };
        }
    }

    private final Map<MoonPhase, Boolean> moonPhaseMap;

    public MoonPhaseCheck(Map<MoonPhase, Boolean> moonPhaseMap) {
        this.moonPhaseMap = moonPhaseMap;
    }

    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        Log.logInfo("Checking moon phase: " + MoonPhase.fromInt(occurrence.getMoonPhaseLevel()) + " in " + moonPhaseMap, Verbosity.HIGHEST);
        return CustomDrop.checkList(MoonPhase.fromInt(occurrence.getMoonPhaseLevel()), moonPhaseMap);
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Map<MoonPhase, Boolean> result = OtherDropsConfig.parseMoonPhaseFrom(parseMe);
        if (result == null || result.isEmpty())
            return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new MoonPhaseCheck(result));
        return conditionList;
    }
}
