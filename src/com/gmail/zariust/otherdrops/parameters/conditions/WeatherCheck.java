package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.Weather;
import com.gmail.zariust.otherdrops.parameters.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeatherCheck extends Condition {
    private final Map<Weather, Boolean> weatherMap;

    public WeatherCheck(Map<Weather, Boolean> weatherMap) {
        this.weatherMap = weatherMap;
    }


    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        if (weatherMap == null || weatherMap.isEmpty())
            return true;
        boolean match = weatherMap.get(null);
        for (Weather type : weatherMap.keySet()) {
            if (type != null) {
                if (type.matches(occurrence.getWeather())) {
                    if (weatherMap.get(type))
                        match = true;
                    else
                        return false;
                }
            }
        }
        return match;
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Map<Weather, Boolean> result = Weather.parseFrom(parseMe, OtherDropsConfig.defaultWeather);
        if(result == null  || result.isEmpty())
            return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new WeatherCheck(result));
        return conditionList;
    }
}
