package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Committable;
import com.gmail.zariust.otherdrops.parameters.Condition;
import com.gmail.zariust.otherdrops.parameters.actions.MessageAction;
import com.gmail.zariust.otherdrops.things.ODVariables;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CooldownCheck extends Condition implements Committable {
    /** Raw cooldown name from the config (may contain variables). Global cooldowns are prefixed with "$global". */
    private final String cooldown;
    private final String cooldownMessage;
    private final Double time;

    String name = "CooldownCheck";

    public CooldownCheck(String cooldown, String cooldownMessage, Double time) {
        this.cooldown = cooldown;
        this.cooldownMessage = cooldownMessage == null ? null : ODVariables.preParse(cooldownMessage);
        this.time = time;
    }

    private boolean isGlobal() {
        return cooldown.startsWith("$global");
    }

    /** Looks up the active cooldown for this event, or null if there is none. Never starts or resets anything. */
    private PlayerCooldown lookup(String resolvedName, Player player) {
        if (isGlobal()) return Cooldown.getGlobalCooldown(resolvedName);
        if (player == null) return null;
        return Cooldown.getCooldown(resolvedName, player.getUniqueId());
    }

    /** CHECK ONLY: is the cooldown free? The timer is started later in commit(). */
    @Override
    public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        // Resolve variables into a local value (previously this overwrote the field, freezing e.g. %p to the first player)
        String resolvedName = MessageAction.parseVariables(cooldown, drop, occurrence, 1);
        occurrence.setCommitData(this, resolvedName);

        Player player = occurrence.getPlayerAttacker();
        if (!isGlobal() && player == null) return true; // per-player cooldowns need a player; nothing to track

        PlayerCooldown pc = lookup(resolvedName, player);
        if (pc == null || pc.isOver()) return true;

        Log.logInfo("Cooldown '" + resolvedName + "' has: " + ((double) pc.getTimeLeft() / 1000) + " seconds left", Verbosity.HIGHEST);
        if (cooldownMessage != null && player != null)
            player.sendMessage(cooldownMessage.replaceAll("%time", String.valueOf(pc.getTimeLeft() / 1000)));
        return false;
    }

    /**
     * Re-check right before running. If another section in the same event shares this cooldown name and
     * already committed, the timer is now active and this section is cancelled (they share one timer).
     */
    @Override
    public boolean canCommit(CustomDrop drop, OccurredEvent occurrence) {
        String resolvedName = (String) occurrence.getCommitData(this);
        if (resolvedName == null) return true;
        Player player = occurrence.getPlayerAttacker();
        if (!isGlobal() && player == null) return true;
        PlayerCooldown pc = lookup(resolvedName, player);
        return pc == null || pc.isOver();
    }

    /** Start (or restart) the timer. */
    @Override
    public void commit(CustomDrop drop, OccurredEvent occurrence) {
        String resolvedName = (String) occurrence.getCommitData(this);
        if (resolvedName == null) return;
        long lengthInMillis = (long) (time * 1000);

        if (isGlobal()) {
            PlayerCooldown pc = Cooldown.getGlobalCooldown(resolvedName);
            if (pc == null) Cooldown.addGlobalCooldown(resolvedName, lengthInMillis);
            else pc.reset();
        } else {
            Player player = occurrence.getPlayerAttacker();
            if (player == null) return;
            PlayerCooldown pc = Cooldown.getCooldown(resolvedName, player.getUniqueId());
            if (pc == null) Cooldown.addCooldown(resolvedName, player.getUniqueId(), lengthInMillis);
            else pc.reset();
        }
    }

    @Override
    public List<Condition> parse(ConfigurationNode node) {
        String cooldown = node.getString("cooldown");
        String globalcooldown = node.getString("globalcooldown");
        String message = node.getString("cooldownmessage");
        List<Condition> conditionList = new ArrayList<>();
        if (cooldown == null && globalcooldown == null) return null;

        if (cooldown != null) {
            String[] split = cooldown.split("@");
            cooldown = split[0];
            double time = 2.0;
            if (split.length > 1) time = Double.parseDouble(split[1]);

            conditionList.add(new CooldownCheck(cooldown, message, time));
        }

        if (globalcooldown != null) {
            String[] split = globalcooldown.split("@");
            globalcooldown = "$global".concat(split[0]);
            double time = 2.0;
            if (split.length > 1) time = Double.parseDouble(split[1]);

            conditionList.add(new CooldownCheck(globalcooldown, message, time));
        }
        return conditionList;
    }
}
