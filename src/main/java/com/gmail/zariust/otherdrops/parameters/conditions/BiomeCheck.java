package main.java.com.gmail.zariust.otherdrops.parameters.conditions;

import main.java.com.gmail.zariust.otherdrops.ConfigurationNode;
import main.java.com.gmail.zariust.otherdrops.OtherDropsConfig;
import main.java.com.gmail.zariust.otherdrops.event.CustomDrop;
import main.java.com.gmail.zariust.otherdrops.event.OccurredEvent;
import main.java.com.gmail.zariust.otherdrops.parameters.Condition;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BiomeCheck extends Condition {
    protected final Map<Biome, Boolean> biomeMap;

    public BiomeCheck(Map<Biome, Boolean> biomeMap) {
        this.biomeMap = biomeMap;
    }

    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        return CustomDrop.checkList(occurrence.getBiome(), biomeMap);
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Map<Biome, Boolean> result = OtherDropsConfig.parseBiomesFrom(parseMe);
        if (result == null || result.isEmpty()) return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new BiomeCheck(result));
        return conditionList;
    }
}
