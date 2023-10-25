package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.Comparative;
import com.gmail.zariust.otherdrops.parameters.Condition;
import com.gmail.zariust.otherdrops.things.ODPotionEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PotionEffectCondition extends Condition {
    public enum PotionTarget {
        ATTACKER, VICTIM
    }

    static Map<String, PotionTarget> matches = new HashMap<>();
    static {
        matches.put("potionrequirement", PotionTarget.ATTACKER);
        matches.put("potionrequirement.attacker", PotionTarget.ATTACKER);
        matches.put("potionrequirement.victim", PotionTarget.VICTIM);
    }

    private final List<ODPotionEffect> potionEffects;

    public PotionEffectCondition(List<ODPotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        if(potionEffects == null || potionEffects.isEmpty())
            return true;

        for(ODPotionEffect potionEffect : potionEffects) {
            LivingEntity target = null;

            if(potionEffect.getTarget() == PotionTarget.ATTACKER)
                target = occurrence.getAttacker();
            else if(potionEffect.getTarget() == PotionTarget.VICTIM)
                target = occurrence.getVictim();

            if(target != null) {
                Log.logInfo("Checking for potion: " + potionEffect + " targetting " + target.getName(), Verbosity.HIGHEST);
                if(potionEffect.getFlag()) { // Potion is required
                    if(!target.hasPotionEffect(potionEffect.getType()))  // Entity does not have effect
                        return false;
                    PotionEffect entityEffect = target.getPotionEffect(potionEffect.getType());
                    if(!potionEffect.getAmplifier().matches(entityEffect.getAmplifier() + 1)) // Amplifier doesn't match
                        return false;
                } else { // Entity SHOULD NOT have potion effect
                    if(target.hasPotionEffect(potionEffect.getType())) { // Player has the effect
                        PotionEffect entityEffect = target.getPotionEffect(potionEffect.getType());
                        if(potionEffect.getAmplifier().matches(entityEffect.getAmplifier() + 1)) { // Make sure amplifier is same
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        List<Condition> conditionList = new ArrayList<Condition>();
        List<ODPotionEffect> potionEffects = new ArrayList<>();

        for (String key : matches.keySet()) {
            List<String> input = OtherDropsConfig.getMaybeList(parseMe, key);
            if (input.isEmpty())
                continue;

            for (String potion : input) {
                PotionEffectType effect;
                Comparative amplifier = null;

                boolean flag = true;
                if (potion.startsWith("-")) {
                    potion = potion.substring(1);
                    flag = false;
                }

                String[] split = potion.split("@");

                if(split.length > 1)
                    amplifier = Comparative.parse(split[1]);

                if(amplifier == null)
                    amplifier = Comparative.parse(">0");

                effect = PotionEffectType.getByName(split[0]);

                if (effect != null) {
                    Log.logInfo("Found potion requirement: " + effect.getName() + " with amplifier " + amplifier + " targeting " + matches.get(key), Verbosity.HIGHEST);
                    potionEffects.add(new ODPotionEffect(effect, flag, amplifier, matches.get(key)));
                }
            }
        }

        if(potionEffects.isEmpty())
            return null;

        conditionList.add(new PotionEffectCondition(potionEffects));
        return conditionList;
    }
}
