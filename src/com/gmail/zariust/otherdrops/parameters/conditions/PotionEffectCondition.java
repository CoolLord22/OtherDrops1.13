package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.options.Comparative;
import com.gmail.zariust.otherdrops.parameters.Condition;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PotionEffectCondition extends Condition {
    private final Map<PotionEffectType, Boolean> potionEffectFlagMap;
    private final Map<PotionEffectType, Comparative> potionEffectAmplifierMap;

    public PotionEffectCondition(Map<PotionEffectType, Boolean> potionEffectFlagMap, Map<PotionEffectType, Comparative> potionEffectAmplifierMap) {
        this.potionEffectFlagMap = potionEffectFlagMap;
        this.potionEffectAmplifierMap = potionEffectAmplifierMap;
    }

    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        if(potionEffectFlagMap == null || potionEffectFlagMap.isEmpty())
            return true;
        Player player = occurrence.getPlayerAttacker();
        for(PotionEffectType effect : potionEffectFlagMap.keySet()) {
            Log.logInfo("Checking for potion: " + effect.getName(), Verbosity.HIGHEST);
            if(potionEffectFlagMap.get(effect)) { // Potion is required
                if(!player.hasPotionEffect(effect))  // Player does not have effect
                    return false;
                PotionEffect playerEffect = player.getPotionEffect(effect);
                if(!potionEffectAmplifierMap.get(effect).matches(playerEffect.getAmplifier() + 1)) // Amplifier doesn't match
                    return false;
            } else { // Player SHOULD NOT have potion effect
                if(player.hasPotionEffect(effect)) { // Player has the effect
                    PotionEffect playerEffect = player.getPotionEffect(effect);
                    if(potionEffectAmplifierMap.get(effect).matches(playerEffect.getAmplifier() + 1)) { // Make sure amplifier is same
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Map<PotionEffectType, Boolean> potionEffectFlag = new HashMap<>();
        Map<PotionEffectType, Comparative> potionEffectAmplifier = new HashMap<>();

        List<String> input = OtherDropsConfig.getMaybeList(parseMe, "potionrequirement");
        if (input.isEmpty())
            return null;

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
                Log.logInfo("Found potion effect: " + effect.getName() + " requiring amplifier " + amplifier, Verbosity.HIGHEST);
                potionEffectFlag.put(effect, flag);
                potionEffectAmplifier.put(effect, amplifier);
            }
        }

        List<Condition> conditionList = new ArrayList<Condition>();
        conditionList.add(new PotionEffectCondition(potionEffectFlag, potionEffectAmplifier));
        return conditionList;
    }
}
