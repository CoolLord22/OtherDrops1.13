package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Condition;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FishhookBiomeCheck extends BiomeCheck {
    public FishhookBiomeCheck(Map<Biome, Boolean> biomeMap) {
        super(biomeMap);
    }

    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        return CustomDrop.checkList(occurrence.getFishingBiome(), biomeMap);
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Map<Biome, Boolean> result = OtherDropsConfig.parseFishingBiomesFrom(parseMe);
        if(result == null || result.isEmpty())
            return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new FishhookBiomeCheck(result));
        return conditionList;
    }
}
