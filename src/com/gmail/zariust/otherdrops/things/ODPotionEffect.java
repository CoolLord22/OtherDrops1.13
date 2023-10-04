package com.gmail.zariust.otherdrops.things;

import com.gmail.zariust.otherdrops.options.Comparative;
import com.gmail.zariust.otherdrops.parameters.conditions.PotionEffectCondition;
import org.bukkit.potion.PotionEffectType;

public class ODPotionEffect {
    private PotionEffectType potionEffectType;
    private Boolean flag;
    private Comparative amplifier;
    private PotionEffectCondition.PotionTarget target;

    public ODPotionEffect(PotionEffectType potionEffectType, Boolean flag, Comparative amplifier, PotionEffectCondition.PotionTarget target) {
        this.potionEffectType = potionEffectType;
        this.flag = flag;
        this.amplifier = amplifier;
        this.target = target;
    }

    public PotionEffectType getType() {
        return potionEffectType;
    }

    public Boolean getFlag() {
        return flag;
    }

    public Comparative getAmplifier() {
        return amplifier;
    }

    public PotionEffectCondition.PotionTarget getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return (flag ? "-" : "") + potionEffectType.getName() + amplifier + "@" + target;
    }
}
