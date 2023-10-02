package com.gmail.zariust.otherdrops.data.entities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.SimpleData;

public class EndermanData extends CreatureData {
    private BlockData 	 bd       = null;
    private Boolean      canCarry = null;

    public EndermanData(BlockData type, Boolean canCarry) {
        this.bd = type;
        this.canCarry = canCarry;
    }

    @SuppressWarnings("unused")
	@Override
    public void setOn(Entity mob, Player owner) {
        if (mob instanceof Enderman) {
            Enderman z = (Enderman) mob;
            if (this.bd != null)
                ((Enderman) mob).setCarriedBlock(bd);
            if (this.canCarry != null)
                ((Enderman) mob).setCanPickupItems(canCarry);
        }
    }

    @Override
    public boolean matches(Data d) {
        if (!(d instanceof EndermanData))
            return false;
        EndermanData vd = (EndermanData) d;

        if (this.bd != null)
            if (this.bd != vd.bd)
                return false;

        if (this.canCarry != null)
            if (this.canCarry != vd.canCarry)
                return false;
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
        Log.logInfo("EndermanData: parsing from string.", Verbosity.HIGHEST);
        BlockData blockData = null;
        Boolean canCarry = null;

        if (!state.isEmpty() && !state.equals("0")) {
            String[] split = state
                    .split(OtherDropsConfig.CreatureDataSeparator);

            for (String sub : split) {
                sub = sub.toLowerCase().replaceAll("[\\s-_]", "");
                if (sub.equalsIgnoreCase("carry"))
                    canCarry = true;
                else if (sub.equalsIgnoreCase("nocarry"))
                    canCarry = false;
                else {
                    // nothing else to check so assume material
                    Material material = null;
                    Data data = new SimpleData();
                    if (sub.contains("@")) {
                        String[] split2 = sub.split("@", 2);
                    	Log.logWarning("Unsupported argument for: " + split2[0] + " with " + split[1]);
                        material = CommonMaterial.matchMaterial(split2[0]);
                    } else {
                        material = CommonMaterial.matchMaterial(sub);
                    }
                    if (material != null)
                    	blockData = Bukkit.createBlockData(material);
                }
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
        if (creature instanceof EntityType)
            return this.toString();
        return "";
    }

}
