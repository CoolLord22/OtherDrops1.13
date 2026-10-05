package com.gmail.zariust.otherdrops.data.mob.traits;

import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.mob.CreatureData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class EndermanData extends CreatureData {
    private final BlockData bd;
    private final Boolean canCarry;

    public EndermanData(BlockData type, Boolean canCarry) {
        this.bd = type;
        this.canCarry = canCarry;
    }

    @SuppressWarnings("unused")
    @Override
    public void setOn(Entity mob, Player owner) {
        if (mob instanceof Enderman z) {
            if (this.bd != null) ((Enderman) mob).setCarriedBlock(bd);
            if (this.canCarry != null) ((Enderman) mob).setCanPickupItems(canCarry);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof EndermanData vd)) return false;
        if (this.bd != null) if (this.bd != vd.bd) return false;
        if (this.canCarry != null) if (this.canCarry != vd.canCarry) return false;
        return true;
    }

    public static CreatureData parseFromEntity(Entity entity) {
        if (entity instanceof Enderman) {
            return new EndermanData(((Enderman) entity).getCarriedBlock(), ((Enderman) entity).getCanPickupItems());
        } else {
            Log.logInfo("EndermanData: error, parseFromEntity given different creature - this shouldn't happen.");
            return null;
        }

    }

    public static CreatureData parseFromString(String state) {
        BlockData blockData = null;
        Boolean canCarry = null;
        for (String sub : CreatureData.keywords(state)) {
            if (sub.equals("carry")) canCarry = true;
            else if (sub.equals("nocarry")) canCarry = false;
            else {
                Material material = CommonMaterial.matchMaterial(sub.split("@", 2)[0]);
                if (material != null && material.isBlock()) blockData = Bukkit.createBlockData(material);
            }
        }
        return new EndermanData(blockData, canCarry);
    }

    @Override
    public String toString() {
        String val = "";
        if (bd != null) {
            val += "!!" + bd.getAsString();
        }
        if (canCarry != null) {
            val += (canCarry ? "!!carry" : "!!nocarry");
        }
        return val;
    }

    @Override
    public String get(Enum<?> creature) {
        if (creature instanceof EntityType) return this.toString();
        return "";
    }

}
