package com.gmail.zariust.otherdrops.parameters;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.actions.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Action extends Parameter {
    protected static final Set<Action> actions = new HashSet<>();

    public abstract boolean act(CustomDrop drop, OccurredEvent occurence);

    public static boolean registerAction(Action register) {
        Log.logInfo("Actions - registering: " + register.toString(), Verbosity.EXTREME);
        actions.add(register);
        return false;
    }

    public static List<Action> parseNodes(ConfigurationNode node) {
        List<Action> actionsList = new ArrayList<>();
        for (Action action : actions) {
            actionsList.addAll(action.parse(node));
        }
        return actionsList;
    }

    abstract public List<Action> parse(ConfigurationNode parseMe);

    public static void registerDefaultActions() {
        registerAction(new DamageAction(null, null));
        registerAction(new MessageAction(null, null));
        registerAction(new MoneyAction(null, null));
        registerAction(new ParticleAction(null, null, true));
        registerAction(new PlayerAction(null, null, null));
        registerAction(new PotionAction(null, null, true));
        registerAction(new SoundAction(null, null));
        registerAction(new SpecialMessageAction(null, null));
    }

    /**
     * Players within `radius` blocks of `loc` (a sphere, same world only).
     * Shared by every action's ".radius" target.
     */
    protected static List<Player> getPlayersInRadius(Location loc, double radius) {
        List<Player> result = new ArrayList<>();
        if (loc == null || loc.getWorld() == null) return result;
        double radiusSquared = radius * radius;
        for (Player player : loc.getWorld().getPlayers())
            if (player.getLocation().distanceSquared(loc) <= radiusSquared) result.add(player);
        return result;
    }
}
