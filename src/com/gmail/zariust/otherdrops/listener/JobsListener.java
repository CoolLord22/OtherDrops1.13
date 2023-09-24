package com.gmail.zariust.otherdrops.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.gamingmesh.jobs.api.JobsExpGainEvent;
import com.gamingmesh.jobs.api.JobsLevelUpEvent;
import com.gamingmesh.jobs.api.JobsPaymentEvent;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.OccurredEvent;

public class JobsListener implements Listener {
    private final OtherDrops parent;

    public JobsListener(OtherDrops instance) {
        parent = instance;
    }
    
    @EventHandler
    public void onJobLevelUp(JobsLevelUpEvent event) {
        if (event.isCancelled())
            return;
        if (!OtherDropsConfig.dropForJobsLevelUp)
            return;
        OccurredEvent drop = new OccurredEvent(event);
        parent.sectionManager.performDrop(drop);
    }
    
    @EventHandler
    public void onJobExpGain(JobsExpGainEvent event) {
        if (event.isCancelled())
            return;
        if (!OtherDropsConfig.dropForJobsExpGain)
            return;
        OccurredEvent drop = new OccurredEvent(event);
        parent.sectionManager.performDrop(drop);
    }
    
    @EventHandler
    public void onJobPayment(JobsPaymentEvent event) {
        if (event.isCancelled())
            return;
        if (!OtherDropsConfig.dropForJobsPayment)
            return;
        OccurredEvent drop = new OccurredEvent(event);
        parent.sectionManager.performDrop(drop);
    }
}
