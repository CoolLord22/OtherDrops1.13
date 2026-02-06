package main.java.com.gmail.zariust.otherdrops.parameters;

import main.java.com.gmail.zariust.common.Verbosity;
import main.java.com.gmail.zariust.otherdrops.ConfigurationNode;
import main.java.com.gmail.zariust.otherdrops.Log;
import main.java.com.gmail.zariust.otherdrops.event.CustomDrop;
import main.java.com.gmail.zariust.otherdrops.event.OccurredEvent;
import main.java.com.gmail.zariust.otherdrops.parameters.actions.*;

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

}
