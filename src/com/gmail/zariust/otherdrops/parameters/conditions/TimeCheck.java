package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.Time;
import com.gmail.zariust.otherdrops.parameters.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimeCheck extends Condition {
    private final Map<Time, Boolean> timeMap;

    public TimeCheck(Map<Time, Boolean> timeMap) {
        this.timeMap = timeMap;
    }

    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        if (timeMap == null || timeMap.isEmpty())
            return true;
        boolean match = false;
        for (Time t : timeMap.keySet()) {
            if (t.contains(occurrence.getTime())) {
                if (timeMap.get(t))
                    match = true;
                else
                    return false;
            }
        }
        return match;
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Map<Time, Boolean> result = Time.parseFrom(parseMe, OtherDropsConfig.defaultTime);
        if(result == null  || result.isEmpty())
            return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new TimeCheck(result));
        return conditionList;
    }
}
