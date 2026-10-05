package com.gmail.zariust.otherdrops.data.mob.traits;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.mob.CreatureData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

public class HorseData extends CreatureData {
    final Horse.Color horseColor; // null = wildcard
    final Horse.Style horseStyle; // null = wildcard

    public HorseData(Horse.Color horseColor, Horse.Style horseStyle) {
        this.horseColor = horseColor;
        this.horseStyle = horseStyle;
    }

    @Override
    public void setOn(Entity mob, Player owner) {
        if (mob instanceof Horse z) {
            if (horseColor != null) z.setColor(horseColor);
            if (horseStyle != null) z.setStyle(horseStyle);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof HorseData vd)) return false;
        if (this.horseColor != null) if (this.horseColor != vd.horseColor) return false;
        if (this.horseStyle != null) if (this.horseStyle != vd.horseStyle) return false;
        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Horse) {
            return new HorseData(((Horse) entity).getColor(), ((Horse) entity).getStyle());
        } else {
            Log.logInfo("HorseData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }
    }

    public static CreatureData parseFromString(String state) {
        Horse.Color thisColor = null;
        Horse.Style thisStyle = null;
        for (String sub : CreatureData.keywords(state)) {
            for (Horse.Color color : Horse.Color.values())
                if (sub.equals("color" + CreatureData.normalize(color.name()))) thisColor = color;
            for (Horse.Style style : Horse.Style.values())
                if (sub.equals("style" + CreatureData.normalize(style.name()))) thisStyle = style;
        }
        return new HorseData(thisColor, thisStyle);
    }

    @Override
    public String toString() {
        String val = "";
        if (horseColor != null) val += "!!" + horseColor;
        if (horseStyle != null) val += "!!" + horseStyle;
        return val;
    }

    @Override
    public String get(Enum<?> creature) {
        if (creature instanceof EntityType) return this.toString();
        return "";
    }

}
