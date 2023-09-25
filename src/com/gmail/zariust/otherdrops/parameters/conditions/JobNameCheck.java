package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gamingmesh.jobs.api.JobsExpGainEvent;
import com.gamingmesh.jobs.api.JobsLevelUpEvent;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobNameCheck extends Condition {
    private final Map<String, Boolean> jobNamesStored;

    public JobNameCheck(Map<String, Boolean> value) {
        this.jobNamesStored = value;
    }

    @Override
    public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        Log.logInfo("JobNameCheck - start", Verbosity.HIGHEST);
        if (occurrence.getEvent() instanceof JobsLevelUpEvent || occurrence.getEvent() instanceof JobsExpGainEvent) {
        	String jobName = occurrence.getJobName();

            Log.logInfo("JobNameCheck - checking: " + jobNamesStored.toString() + " vs actual: " + jobName, Verbosity.HIGHEST);
            return CustomDrop.checkList(jobName.toUpperCase(), jobNamesStored);
        } else {
            Log.logInfo("JobNameCheck - failed, not an instance of job level up.", Verbosity.HIGHEST);
            return false;
        }
    }

    @Override
    public List<Condition> parse(ConfigurationNode node) {
        Map<String, Boolean> value = new HashMap<String, Boolean>();
        value = parseConfig(node, null);
        if (value == null)
            return null;

        List<Condition> conditionList = new ArrayList<Condition>();
        conditionList.add(new JobNameCheck(value));
        return conditionList;
    }

    public static Map<String, Boolean> parseConfig(ConfigurationNode node, Map<String, Boolean> def) {
        List<String> jobs = OtherDropsConfig.getMaybeList(node, "jobs");
        if (jobs.isEmpty())
            return def;
        HashMap<String, Boolean> result = new HashMap<String, Boolean>();
        result.put(null, OtherDropsConfig.containsAll(jobs));
        for (String name : jobs) {
            name = name.toUpperCase();
            if (name.startsWith("-")) {
                result.put(null, true);
                result.put(name.substring(1), false);
            } else {
                result.put(name, true);
            }
        }
        return result;
    }

}
