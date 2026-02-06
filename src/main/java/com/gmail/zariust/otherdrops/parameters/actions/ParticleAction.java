package main.java.com.gmail.zariust.otherdrops.parameters.actions;

import main.java.com.gmail.zariust.common.Verbosity;
import main.java.com.gmail.zariust.otherdrops.ConfigurationNode;
import main.java.com.gmail.zariust.otherdrops.Log;
import main.java.com.gmail.zariust.otherdrops.event.CustomDrop;
import main.java.com.gmail.zariust.otherdrops.event.OccurredEvent;
import main.java.com.gmail.zariust.otherdrops.event.SimpleDrop;
import main.java.com.gmail.zariust.otherdrops.parameters.Action;
import main.java.com.gmail.zariust.otherdrops.subject.CreatureSubject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class ParticleAction extends Action {
    // "particleeffect: " message.player, message.radius@<r>, message.world, message.server
    public enum ParticleEffectActionType {
        ATTACKER, VICTIM, RADIUS, WORLD, SERVER, DROP
    }

    static final Map<String, ParticleEffectActionType> matches = new HashMap<>();

    static {
        matches.put("particleeffect", ParticleEffectActionType.ATTACKER);
        matches.put("particleeffect.attacker", ParticleEffectActionType.ATTACKER);
        matches.put("particleeffect.victim", ParticleEffectActionType.VICTIM);
        matches.put("particleeffect.target", ParticleEffectActionType.VICTIM);
        matches.put("particleeffect.server", ParticleEffectActionType.SERVER);
        matches.put("particleeffect.world", ParticleEffectActionType.WORLD);
        matches.put("particleeffect.global", ParticleEffectActionType.SERVER);
        matches.put("particleeffect.all", ParticleEffectActionType.SERVER);
        matches.put("particleeffect.radius", ParticleEffectActionType.RADIUS);
        matches.put("particleeffect.drop", ParticleEffectActionType.DROP);

        matches.put("particleeffects", ParticleEffectActionType.ATTACKER);
        matches.put("particleeffects.attacker", ParticleEffectActionType.ATTACKER);
        matches.put("particleeffects.victim", ParticleEffectActionType.VICTIM);
        matches.put("particleeffects.target", ParticleEffectActionType.VICTIM);
        matches.put("particleeffects.server", ParticleEffectActionType.SERVER);
        matches.put("particleeffects.world", ParticleEffectActionType.WORLD);
        matches.put("particleeffects.global", ParticleEffectActionType.SERVER);
        matches.put("particleeffects.all", ParticleEffectActionType.SERVER);
        matches.put("particleeffects.radius", ParticleEffectActionType.RADIUS);
        matches.put("particleeffects.drop", ParticleEffectActionType.DROP);

    }

    protected ParticleEffectActionType particleEffectActionType;
    protected final double radius = 10; // default to 10 blocks
    private Collection<ParticleEffect> effects = new ArrayList<>();

    public ParticleAction(Collection<ParticleEffect> effectsList) {
        this.effects = effectsList;
    }

    public ParticleAction(Object object, ParticleEffectActionType particleEffectType2, boolean onlyRemove) {
        this.particleEffectActionType = particleEffectType2;

        if (object instanceof List) {
            @SuppressWarnings("unchecked") List<String> stringList = (List<String>) object;
            for (String effect : stringList) {
                effects.add(getEffect(effect));
            }
        } else if (object instanceof String stringObj) {
            effects.add(getEffect(stringObj));
        }
    }

    @Override
    public boolean act(CustomDrop drop, OccurredEvent occurence) {
        switch (particleEffectActionType) {
            case ATTACKER:
                if (occurence.getPlayerAttacker() != null & this.effects != null)
                    applyEffect(occurence.getPlayerAttacker());
                return false;
            case VICTIM:
                if (occurence.getPlayerVictim() != null & this.effects != null)
                    applyEffect(occurence.getPlayerVictim());
                else if (occurence.getTarget() instanceof CreatureSubject) {
                    Entity ent = ((CreatureSubject) occurence.getTarget()).getEntity();
                    if (ent instanceof LivingEntity) {
                        applyEffect(ent);
                    }
                }

                return false;

            case RADIUS:
                // occurence.getLocation().getRadiusPlayers()? - how do we get players around radius without an entity?
                Location loc = occurence.getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().getX() > (loc.getX() - radius) || player.getLocation().getX() < (loc.getX() + radius))
                        if (player.getLocation().getY() > (loc.getY() - radius) || player.getLocation().getY() < (loc.getY() + radius))
                            if (player.getLocation().getZ() > (loc.getZ() - radius) || player.getLocation().getZ() < (loc.getZ() + radius))
                                applyEffect(player);
                }
                break;
            case SERVER:
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    applyEffect(player);
                }
                break;
            case WORLD:
                for (Player player : occurence.getLocation().getWorld().getPlayers()) {
                    applyEffect(player);
                }
                break;
            case DROP:
                if (drop instanceof SimpleDrop) {
                    applyEffect(occurence.getLocation());
                }
                break;
            default:
                break;
        }

        return false;
    }

    private void applyEffect(Location location) {
        for (ParticleEffect effect : this.effects) {
            try {
                Log.dMsg("Sending effect: " + effect.getType().getName() + " speed: " + effect.getSpeed() + ", count:" + effect.getCount() + ", radius:" + effect.getRadius());
                effect.sendToLocation(location, effect.getSpeed(), effect.getCount(), effect.getRadius());
            } catch (Exception e) {
                Log.logError("Error while applying particle action:", e);
            }
        }

    }

    private void applyEffect(Entity lEnt) {
        for (ParticleEffect effect : this.effects) {
            try {
                Log.dMsg("Sending effect: " + effect.getType().getName() + " speed: " + effect.getSpeed() + ", count:" + effect.getCount() + ", radius:" + effect.getRadius());
                Location location = lEnt.getLocation();
                effect.sendToLocation(location, effect.getSpeed(), effect.getCount(), effect.getRadius());
            } catch (Exception e) {
                Log.logError("Error while applying particle action:", e);
            }
        }
    }

    // @Override
    @Override
    public List<Action> parse(ConfigurationNode parseMe) {
        List<Action> actions = new ArrayList<>();

        for (String key : matches.keySet()) {
            boolean onlyRemove;
            if (parseMe.get(key) != null) {
                onlyRemove = false;
                actions.add(new ParticleAction(parseMe.get(key), matches.get(key), onlyRemove));
            }
        }
        return actions;
    }

    private static ParticleEffect getEffect(String effects) {
        String[] split = effects.split("@");
        double speed = 1;
        int count = 1;
        double radius = 1;

        try {
            if (split.length > 1) speed = Double.parseDouble(split[1]);
        } catch (NumberFormatException ex) {
            Log.logInfo("Particleeffect: invalid speed (" + split[1] + ")");
        }

        try {
            if (split.length > 2) count = Integer.parseInt(split[2]);
        } catch (NumberFormatException ex) {
            Log.logInfo("Particleeffect: invalid count (" + split[2] + ")");
        }

        try {
            if (split.length > 3) radius = Double.parseDouble(split[3]);
        } catch (NumberFormatException ex) {
            Log.logInfo("Particleeffect: invalid radius (" + split[3] + ")");
        }
        ParticleEffect effect = new ParticleEffect(ParticleEffect.ParticleType.valueOf(split[0]));

        effect.setSpeed(speed);
        effect.setCount(count);
        effect.setRadius(radius);

        Log.logInfo("ParticleEffect: adding effect (" + split[0] + ", speed: " + speed + ", count: " + count + ")", Verbosity.HIGH);
        return effect;
    }
}
