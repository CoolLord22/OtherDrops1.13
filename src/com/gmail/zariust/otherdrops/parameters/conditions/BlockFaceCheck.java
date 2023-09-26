package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Condition;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gmail.zariust.common.CommonPlugin.enumValue;

public class BlockFaceCheck extends Condition {
    private final Map<BlockFace, Boolean> facesMap;

    public BlockFaceCheck(Map<BlockFace, Boolean> facesMap) {
        this.facesMap = facesMap;
    }

    @Override
    protected boolean checkInstance(CustomDrop drop, OccurredEvent occurrence) {
        return CustomDrop.checkList(occurrence.getFace(), facesMap);
    }

    @Override
    public List<Condition> parse(ConfigurationNode parseMe) {
        Map<BlockFace, Boolean> result = parseFacesFrom(parseMe);
        if(result == null || result.isEmpty())
            return null;
        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(new BlockFaceCheck(result));
        return conditionList;
    }

    private Map<BlockFace, Boolean> parseFacesFrom(ConfigurationNode node) {
        List<String> faces = OtherDropsConfig.getMaybeList(node, "face", "faces");
        if (faces.isEmpty())
            return null;
        HashMap<BlockFace, Boolean> result = new HashMap<>();
        result.put(null, OtherDropsConfig.containsAll(faces));
        for (String name : faces) {
            BlockFace face = enumValue(BlockFace.class, name.toUpperCase());
            if (face == null && name.startsWith("-")) {
                result.put(null, true);
                face = enumValue(BlockFace.class, name.substring(1).toUpperCase());
                if (face == null) {
                    Log.logWarning("Invalid block face " + name + "; skipping...");
                    continue;
                }
                result.put(face, false);
            } else
                result.put(face, true);
        }
        if (result.isEmpty())
            return null;
        return result;
    }
}
