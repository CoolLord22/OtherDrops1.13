package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldCheck extends Condition {
    private final Map<String, Boolean> worldMap;

    public WorldCheck(Map<String, Boolean> worldMap) {
        this.worldMap = worldMap;
    }

    @Override
    public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        return CustomDrop.checkList(occurrence.getWorld().getName(), worldMap);
    }
    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Map<String, Boolean> result = OtherDropsConfig.parseWorldsFrom(parseMe);
        if(result == null || result.isEmpty())
            return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new WorldCheck(result));
        return conditionList;
    }

}
