package com.gmail.zariust.otherdrops.data.effects;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.EffectData;
import org.bukkit.Effect;
import org.bukkit.block.BlockFace;

import static com.gmail.zariust.common.CommonPlugin.enumValue;

public class SmokeEffectData extends EffectData {

    public SmokeEffectData(BlockFace face) {
        switch (face) {
            case EAST:
                data = 3;
                break;
            case NORTH:
                data = 7;
                break;
            case NORTH_EAST:
                data = 6;
                break;
            case NORTH_WEST:
                data = 8;
                break;
            case SOUTH:
                data = 1;
                break;
            case SOUTH_EAST:
                data = 0;
                break;
            case SOUTH_WEST:
                data = 2;
                break;
            case WEST:
                data = 5;
                break;
            default:
                data = 4;
        }
    }

    public static EffectData parse(String key) {
        BlockFace face = SmokeEffectData.getFaceFromString(key);
        if (face == null) {
            // default to random if no data specified
            return new EffectData(OtherDrops.rng.nextInt(9));
        }
        return new SmokeEffectData(face);

    }

    public static BlockFace getFaceFromString(String key) {
        if (key.isEmpty()) {
            return null;
        }
        return enumValue(BlockFace.class, key);
    }

    @Override
    protected String get(Effect effect) {
        return getDirection().toString();
    }

    public BlockFace getDirection() {
        return switch (data) {
            case 0 -> BlockFace.SOUTH_EAST;
            case 1 -> BlockFace.SOUTH;
            case 2 -> BlockFace.SOUTH_WEST;
            case 3 -> BlockFace.EAST;
            case 4 -> BlockFace.UP;
            case 5 -> BlockFace.WEST;
            case 6 -> BlockFace.NORTH_EAST;
            case 7 -> BlockFace.NORTH;
            case 8 -> BlockFace.NORTH_WEST;
            default -> BlockFace.SELF;
        };
    }
}
