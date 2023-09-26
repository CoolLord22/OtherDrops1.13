package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.Comparative;
import com.gmail.zariust.otherdrops.parameters.Condition;

import java.util.ArrayList;
import java.util.List;

public class AttackRangeCheck extends Condition {
    private final Comparative attackRange;

    public AttackRangeCheck(Comparative attackRange) {
        this.attackRange = attackRange;
    }

    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        if (attackRange == null)
            return true;
        return attackRange.matches(occurrence.getLightLevel());
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Comparative result = Comparative.parseFrom(parseMe, "attackrange", OtherDropsConfig.defaultAttackRange);
        if(result == null)
            return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new AttackRangeCheck(result));
        return conditionList;
    }
}
