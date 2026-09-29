package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Condition;

import java.util.*;

public class RegionCheck extends Condition {
    private final Map<String, Boolean> regionMap;

    public RegionCheck(Map<String, Boolean> regions) {
        this.regionMap = regions;
    }

    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        if (regionMap == null) return true;

        Set<String> inRegions = occurrence.getRegions();

        // set matched flag to false, since we know there's at least something in the customRegion condition
        boolean matchedRegion = false;
        int positiveRegions = 0;

        // loop through each region within the customRegions and check if it matches all current regions
        for (Map.Entry<String, Boolean> entry : regionMap.entrySet()) {
            if (entry.getKey() == null) continue;
            String dropRegion = entry.getKey().toLowerCase();
            boolean exception = !entry.getValue();
            if (!exception) positiveRegions++;

            if (exception) {
                if (inRegions.contains(dropRegion)) return false;
            } else if (inRegions.contains(dropRegion)) {
                matchedRegion = true;
            }
        }

        // If there were only exception conditions then return true as we haven't been kicked by a matched exception
        if (positiveRegions < 1) matchedRegion = true;

        return matchedRegion;
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Map<String, Boolean> result = OtherDropsConfig.parseRegionsFrom(parseMe);
        if (result == null || result.isEmpty()) return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new RegionCheck(result));
        return conditionList;
    }
}
