package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Condition;
import com.gmail.zariust.otherdrops.subject.BlockTarget;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockPlaceByCheck extends Condition {

    String name = "BlockPlaceByCheck";
    private final Map<String, Boolean> placeByStored;

    public BlockPlaceByCheck(Map<String, Boolean> value) {
        this.placeByStored = value;
    }

    @Override
    public boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
    	Block block = null;
        Log.logInfo("BlockPlaceByCheck - start", Verbosity.HIGHEST);
        
        if (occurrence.getTarget() instanceof BlockTarget) {
        	block = ((BlockTarget) occurrence.getTarget()).getBlock();	
        }
        if (block != null) {
        	String placeBy = "";
        	final PersistentDataContainer customBlockData = new CustomBlockData(block, OtherDrops.plugin);
        	if (!customBlockData.has(OtherDrops.playerPlacedKey, PersistentDataType.STRING)) {
        		placeBy = "NATURAL";
        	} else {
        		placeBy = customBlockData.get(OtherDrops.playerPlacedKey, PersistentDataType.STRING);
        	}
            Log.logInfo(
                    "BlockPlaceByCheck - checking: " + placeByStored.toString()
                            + " vs actual: " + placeBy, Verbosity.HIGHEST);
            return CustomDrop.checkList(placeBy.toUpperCase(), placeByStored);
        } else {
            Log.logInfo("BlockPlaceByCheck - failed, no block target.", Verbosity.HIGHEST);
            return false;
        }
    }

    @Override
    public List<Condition> parse(ConfigurationNode node) {
        Map<String, Boolean> value;
        value = parseConfig(node, null);
        if (value == null)
            return null;

        List<Condition> conditionList = new ArrayList<Condition>();
        conditionList.add(new BlockPlaceByCheck(value));
        return conditionList;
    }

    public static Map<String, Boolean> parseConfig(ConfigurationNode node, Map<String, Boolean> def) {
        List<String> placedBy = OtherDropsConfig.getMaybeList(node, "placedby");
        if (placedBy.isEmpty())
            return def;
        HashMap<String, Boolean> result = new HashMap<String, Boolean>();
        result.put(null, OtherDropsConfig.containsAll(placedBy));
        for (String name : placedBy) {
            name = name.toUpperCase();
            if (name.startsWith("-")) {
                result.put(null, true);
                result.put(name.substring(1), false);
            } else {
                result.put(name, true);
            }
        }
        return result;
    }

}
