package com.gmail.zariust.otherdrops.data.effects;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.EffectData;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import java.util.EnumSet;

public class StepSoundEffectData extends EffectData {
    public StepSoundEffectData(Material mat) { // BLOCK_BREAK effect
        data = mat.getId();
    }

    public static StepSoundEffectData parse(String key) {
        Material mat = Material.getMaterial(key);
        if (mat == null) return null;
        if (mat.isBlock()) return new StepSoundEffectData(mat);

        Log.logInfo("Invalid STEP_SOUND material - not a block.", Verbosity.HIGH);
        return null;


    }

    @Override
    protected String get(Effect effect) {
        String returnVal = "";
        for (Material i : EnumSet.allOf(Material.class)) {
            if (i.getId() == data) returnVal = Bukkit.getUnsafe().fromLegacy(new MaterialData(i)).toString();
        }
        return returnVal;
    }
}
