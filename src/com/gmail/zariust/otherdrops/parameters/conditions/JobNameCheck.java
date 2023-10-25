package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.api.JobsExpGainEvent;
import com.gamingmesh.jobs.api.JobsLevelUpEvent;
import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobProgression;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Dependencies;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.Comparative;
import com.gmail.zariust.otherdrops.parameters.Condition;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobNameCheck extends Condition {
    private final Map<String, Boolean> jobNamesStored;
    private final Map<String, Comparative> jobLevelsStored;

    public JobNameCheck(Map<String, Boolean> value, Map<String, Comparative> jobLevelsStored) {
        this.jobNamesStored = value;
        this.jobLevelsStored = jobLevelsStored;
    }

    @Override
    public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        if(Dependencies.hasJobs()) {
            if (occurrence.getEvent() instanceof JobsLevelUpEvent || occurrence.getEvent() instanceof JobsExpGainEvent) {
                if(!(occurrence.getTarget() instanceof PlayerSubject p)) {
                    Log.logInfo("No player target found for jobs event.", Verbosity.HIGHEST);
                    return true;
                }
                if(jobNamesStored.containsKey(occurrence.getJobName())) {
                    Job job = Jobs.getJob(occurrence.getJobName());
                    return checkJob(jobNamesStored.get(occurrence.getJobName()), p.getPlayer(), job);
                }
                return false; // assume false if not specified?
            } else {
                Player target = null;
                if(occurrence.getAttacker() instanceof Player)
                    target = (Player) occurrence.getAttacker();
                else if(occurrence.getVictim() instanceof Player)
                    target = (Player) occurrence.getVictim();

                if(target != null) {
                    Log.logInfo("JobNameCheck - Starting job checks for " + target.getName(), Verbosity.HIGHEST);
                    for(Map.Entry<String, Boolean> entry : jobNamesStored.entrySet()) {
                        if(entry.getKey() == null)
                            continue;
                        Job job = Jobs.getJob(entry.getKey());
                        if(!checkJob(entry.getValue(), target, job))
                            return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean checkJob(boolean requirement, Player target, Job jobToCheck) {
        if(requirement) { // Player has to be in the job
            if(!Jobs.getPlayerManager().getJobsPlayer(target).isInJob(jobToCheck)) {
                Log.logInfo("JobNameCheck - Player was not found in target job: " + jobToCheck.getName(), Verbosity.HIGHEST);
                return false;
            }
            JobProgression jobProgress = Jobs.getPlayerManager().getJobsPlayer(target).getJobProgression(jobToCheck);
            Log.logInfo("JobNameCheck - Checking player job level: " + jobProgress.getLevel() + " vs requirement: " + jobLevelsStored.get(jobToCheck.getName()) + " for " + jobToCheck.getName(), Verbosity.HIGHEST);
            return jobLevelsStored.get(jobToCheck.getName()).matches(jobProgress.getLevel());
        } else { // Player should not be in job
            if(Jobs.getPlayerManager().getJobsPlayer(target).isInJob(jobToCheck)) {
                JobProgression jobProgress = Jobs.getPlayerManager().getJobsPlayer(target).getJobProgression(jobToCheck);
                Log.logInfo("JobNameCheck - Player was found in target job: " + jobToCheck.getName() + " checking level exception: " + jobProgress.getLevel() + " vs requirement: " + jobLevelsStored.get(jobToCheck.getName()), Verbosity.HIGHEST);
                return !jobLevelsStored.get(jobToCheck.getName()).matches(jobProgress.getLevel());
            }
        }
        return true;
    }

    @Override
    public List<Condition> parse(ConfigurationNode node) {
        List<String> jobs = OtherDropsConfig.getMaybeList(node, "jobs");

        if(jobs.isEmpty())
            return null;

        HashMap<String, Boolean> names = new HashMap<>();
        HashMap<String, Comparative> levels = new HashMap<>();

        names.put(null, OtherDropsConfig.containsAll(jobs));

        for (String name : jobs) {
            boolean flag = true;
            Comparative level = null;
            String[] split = name.split("@");

            if(split.length > 1)
                level = Comparative.parse(split[1]);

            if(level == null)
                level = Comparative.parse(">0");

            String tempJob = split[0];
            if (split[0].startsWith("-")) {
                flag = false;
                tempJob = tempJob.substring(1);
            }

            String finalJobName = null;
            for(Job job : Jobs.getJobs()) {
                if(job.getName().equalsIgnoreCase(tempJob))
                    finalJobName = job.getName();
            }

            if(finalJobName != null) {
                names.put(finalJobName, flag);
                levels.put(finalJobName, level);
            } else {
                Log.logWarning("Invalid Job specified: " + tempJob + " please re-check job name!");
            }
        }

        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new JobNameCheck(names, levels));
        return conditionList;
    }
}
