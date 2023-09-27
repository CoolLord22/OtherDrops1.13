package com.gmail.zariust.otherdrops.data.itemmeta;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.parameters.actions.PotionAction;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class OdPotionMeta extends OdItemMeta {
    public List<PotionEffect> potionEffectList;
    public Color potionColor;

    public OdPotionMeta(List<PotionEffect> potionEffectList, Color potionColor) {
        this.potionEffectList = potionEffectList;
        this.potionColor = potionColor;
    }

    @Override
    public ItemStack setOn(ItemStack stack, Target source) {
        PotionMeta meta = (PotionMeta) stack.getItemMeta();
        meta.clearCustomEffects();
        for(PotionEffect effect : potionEffectList)  {
            meta.addCustomEffect(effect, true);
        }
        if(potionColor != null)
            meta.setColor(potionColor);
        stack.setItemMeta(meta);
        return stack;
    }

    public static OdPotionMeta parse(String sub) {
        List<PotionEffect> potionEffects = new ArrayList<>();
        Color potionColor = null;
        for(String effect : sub.split("!")) {
            PotionEffect singleEffect = PotionAction.getEffect(effect);
            if(singleEffect != null)
                potionEffects.add(singleEffect);
            else {
                try {
                    Log.logInfo("Trying to get color: " + effect, Verbosity.HIGHEST);
                    potionColor = DyeColor.valueOf(effect.toUpperCase()).getColor();
                } catch (IllegalArgumentException e) {
                    Log.logInfo("Invalid potion data was passed, ignoring: " + effect, Verbosity.HIGHEST);
                }
            }
        }
        return new OdPotionMeta(potionEffects, potionColor);
    }
}
