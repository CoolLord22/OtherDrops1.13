package com.gmail.zariust.otherdrops.data;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Dependencies;
import com.gmail.zariust.otherdrops.Log;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class MythicMobData implements Data {
    String mythicMobType = null;

    public MythicMobData(String mythicMobType) {
        if(Dependencies.hasMythicMobs()) {
            if(!mythicMobType.equalsIgnoreCase("ANY") && !Dependencies.getMythicMobs().getMobManager().getMythicMob(mythicMobType).isPresent()) {
                Log.logInfo("Invalid mythic mob specified/could not be found: " + mythicMobType, Verbosity.HIGHEST);
                return;
            }
            this.mythicMobType = mythicMobType;
        }
    }

    @Override
    public int getData() {
        return 0;
    }

    @Override
    public void setData(int d) { }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof MythicMobData))
            return false;

        if(this.mythicMobType != null) {
            if(this.mythicMobType.equalsIgnoreCase("ANY") || ((MythicMobData) d).mythicMobType.equalsIgnoreCase("ANY"))
                return true;
            return this.mythicMobType.equalsIgnoreCase(((MythicMobData) d).mythicMobType);
        }

        return true;
    }

    @Override
    public String get(Enum<?> mat) {
        if (mat instanceof EntityType)
            return this.toString();
        return "";
    }

    @Override
    public void setOn(BlockState state) { }

    @Override
    public void setOn(Entity entity, Player witness) {

    }

    @Override
    public Boolean getSheared() {
        return null;
    }

    public String getMythicMobType() {
        return mythicMobType;
    }
    @Override
    public String toString() {
        String val = "MYTHIC_MOB@";
        if (mythicMobType != null) {
            val += mythicMobType;
        }
        return val;
    }
}
