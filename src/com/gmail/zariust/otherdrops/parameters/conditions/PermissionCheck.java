package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Dependencies;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Condition;
import com.gmail.zariust.otherdrops.subject.Agent;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.ProjectileAgent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionCheck extends Condition {
    private final Map<String, Boolean> permissionMap;

    public PermissionCheck(Map<String, Boolean> permissionMap) {
        this.permissionMap = permissionMap;
    }

    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        if (permissionMap == null)
            return true;
        Agent agent = occurrence.getTool();
        Player player = null;

        if (!(agent instanceof PlayerSubject)) {
            if (agent instanceof ProjectileAgent) {
                Entity shooter = ((ProjectileAgent) agent).getShooter()
                        .getEntity();
                if (shooter instanceof Player) {
                    player = (Player) shooter;
                }
            }
            if (player == null)
                return false; // if permissions is set and agent (or shooter) is
            // not a player, fail
        }

        if (player == null)
            player = ((PlayerSubject) agent).getPlayer();

        boolean match = false;
        for (String perm : permissionMap.keySet()) {
            if (perm.startsWith("!")) {
                perm = perm.substring(1);

                if (Dependencies.hasPermission(player, perm)) {
                    if (permissionMap.get(perm))
                        match = true;
                    else {
                        return false;
                    }
                }
            }

            else {
                if (Dependencies.hasPermission(player, "otherdrops.custom." + perm)) {
                    if (permissionMap.get(perm))
                        match = true;
                    else {
                        return false;
                    }
                }
            }
        }
        return match;
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Map<String, Boolean> result = OtherDropsConfig.parsePermissionsFrom(parseMe);
        if(result == null || result.isEmpty())
            return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new PermissionCheck(result));
        return conditionList;
    }
}
