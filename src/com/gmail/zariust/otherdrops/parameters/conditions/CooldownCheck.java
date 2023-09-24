package com.gmail.zariust.otherdrops.parameters.conditions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Condition;
import com.gmail.zariust.otherdrops.parameters.actions.MessageAction;

public class CooldownCheck extends Condition {
    private String       cooldown;
    private String       cooldownMessage;
    private final Double time;

    String               name = "CooldownCheck";

    public CooldownCheck(String cooldown, String cooldownMessage, Double time) {
        this.cooldown = cooldown;
        this.cooldownMessage = cooldownMessage;
        this.time = time;
    }

    @Override
    public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        cooldown = MessageAction.parseVariables(cooldown, drop, occurrence, 1);
        if(cooldown.startsWith("$global")) {
            PlayerCooldown pc = Cooldown.getGlobalCooldown(cooldown);
            if (pc == null) {// The player hasn't activated a cooldown for this yet
                // name for cooldown, length of cooldown in milliseconds
                Cooldown.addGlobalCooldown(cooldown, (long) (time * 1000));
                return true;
            } else {
                if (pc.isOver()) {
                    pc.reset();
                    return true;
                } else {
                    Log.logInfo("Cooldown '" + cooldown + "' has: " + ((double) pc.getTimeLeft() / 1000) + "Seconds left", Verbosity.HIGHEST);
                    if(cooldownMessage != null)
                    	occurrence.getPlayerAttacker().sendMessage(cooldownMessage.replaceAll("%time", String.valueOf(pc.getTimeLeft() / 1000)));
                    return false;
                }
            }
        }
        else {
            PlayerCooldown pc = Cooldown.getCooldown(cooldown, occurrence.getPlayerAttacker().getUniqueId());
            if (pc == null) {// The player hasn't activated a cooldown for this yet
                // name for cooldown, length of cooldown in milliseconds
                Cooldown.addCooldown(cooldown, occurrence.getPlayerAttacker().getUniqueId(), (long) (time * 1000));
                return true;
            } else {
                if (pc.isOver()) {
                    pc.reset();
                    return true;
                } else {
                    Log.logInfo("Cooldown '" + cooldown + "' for '" + Bukkit.getPlayer(pc.getUUID()) + "' has: " + ((double) pc.getTimeLeft() / 1000) + "Seconds left", Verbosity.HIGHEST);
                    if(cooldownMessage != null)
                    	occurrence.getPlayerAttacker().sendMessage(cooldownMessage.replaceAll("%time", String.valueOf(pc.getTimeLeft() / 1000)));
                    return false;
                }
            }
        }
    }

    @Override
    public List<Condition> parse(ConfigurationNode node) {
        String cooldown = node.getString("cooldown");
        String globalcooldown = node.getString("globalcooldown");
        String message = node.getString("cooldownmessage");
        List<Condition> conditionList = new ArrayList<Condition>();
        if (cooldown == null && globalcooldown == null)
        	return null;
        
        if (cooldown != null) {
            String[] split = cooldown.split("@");
            cooldown = split[0];
            Double time = 2.0;
            if (split.length > 1)
                time = Double.valueOf(split[1]);

            conditionList.add(new CooldownCheck(cooldown, message, time));
        }

        if (globalcooldown != null) {
            String[] split = globalcooldown.split("@");
            globalcooldown = "$global".concat(split[0]);
            Double time = 2.0;
            if (split.length > 1)
                time = Double.valueOf(split[1]);

            conditionList.add(new CooldownCheck(globalcooldown, message, time));
        }
        return conditionList;
    }
}
