package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.Comparative;
import com.gmail.zariust.otherdrops.parameters.Condition;

import java.util.ArrayList;
import java.util.List;

public class LightLevelCheck extends Condition {
    private final Comparative lightLevel;

    public LightLevelCheck(Comparative lightLevel) {
        this.lightLevel = lightLevel;
    }

    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        if (lightLevel == null)
            return true;
        return lightLevel.matches(occurrence.getLightLevel());
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Comparative result = Comparative.parseFrom(parseMe, "lightlevel", OtherDropsConfig.defaultLightLevel);
        if(result == null)
            return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new LightLevelCheck(result));
        return conditionList;
    }
}
