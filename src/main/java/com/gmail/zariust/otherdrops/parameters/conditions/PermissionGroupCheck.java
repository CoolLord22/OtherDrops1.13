package main.java.com.gmail.zariust.otherdrops.parameters.conditions;

import main.java.com.gmail.zariust.otherdrops.ConfigurationNode;
import main.java.com.gmail.zariust.otherdrops.OtherDrops;
import main.java.com.gmail.zariust.otherdrops.OtherDropsConfig;
import main.java.com.gmail.zariust.otherdrops.event.CustomDrop;
import main.java.com.gmail.zariust.otherdrops.event.OccurredEvent;
import main.java.com.gmail.zariust.otherdrops.parameters.Condition;
import main.java.com.gmail.zariust.otherdrops.subject.Agent;
import main.java.com.gmail.zariust.otherdrops.subject.PlayerSubject;
import main.java.com.gmail.zariust.otherdrops.subject.ProjectileAgent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionGroupCheck extends Condition {
    private final Map<String, Boolean> permissionGroupMap;

    public PermissionGroupCheck(Map<String, Boolean> permissionGroupMap) {
        this.permissionGroupMap = permissionGroupMap;
    }

    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        if (permissionGroupMap == null) return true;
        Agent agent = occurrence.getTool();
        Player player = null;

        if (!(agent instanceof PlayerSubject)) {
            if (agent instanceof ProjectileAgent) {
                Entity shooter = ((ProjectileAgent) agent).getShooter().getEntity();
                if (shooter instanceof Player) {
                    player = (Player) shooter;
                }
            } else return false; // if permissions is set and agent is not a player, fail
        }

        if (player == null) player = ((PlayerSubject) agent).getPlayer();

        boolean match = false;
        for (String group : permissionGroupMap.keySet()) {
            if (OtherDrops.inGroup(player, group)) {
                if (permissionGroupMap.get(group)) match = true;
                else return false;
            }
        }
        return match;
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Map<String, Boolean> result = OtherDropsConfig.parseGroupsFrom(parseMe);
        if (result == null || result.isEmpty()) return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new PermissionGroupCheck(result));
        return conditionList;
    }
}
