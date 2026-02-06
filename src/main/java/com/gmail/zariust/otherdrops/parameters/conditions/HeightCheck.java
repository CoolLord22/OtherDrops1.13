package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.Comparative;
import com.gmail.zariust.otherdrops.parameters.Condition;

import java.util.ArrayList;
import java.util.List;

public class HeightCheck extends Condition {
    private final Comparative height;

    public HeightCheck(Comparative height) {
        this.height = height;
    }

    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        if (height == null) return true;
        return height.matches(occurrence.getLightLevel());
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Comparative result = Comparative.parseFrom(parseMe, "height", OtherDropsConfig.defaultHeight);
        if (result == null) return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new HeightCheck(result));
        return conditionList;
    }
}
