package com.gmail.zariust.otherdrops.data;

import com.gmail.zariust.otherdrops.Log;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Block state data written the same way as vanilla commands and the F3 screen:
 * WHEAT@[age=7], BEEHIVE@[honey_level=5], OAK_STAIRS@[facing=north,half=top].
 * <p>
 * Only the properties listed in the config are compared (via BlockData#matches), so [age=7] matches
 * fully grown wheat regardless of any other property. Works for every block state Minecraft has,
 * with no per-block code.
 */
public class BlockStateData implements Data {
    /** Parsed with Bukkit.createBlockData(material, "[...]"): only the listed properties count as "set". */
    private final BlockData state;

    public BlockStateData(BlockData state) {
        this.state = state;
    }

    /** True if the value looks like "[property=value,...]". */
    public static boolean isBlockState(String value) {
        return value != null && value.startsWith("[") && value.endsWith("]");
    }

    /**
     * Parses "[property=value,...]" for the given block.
     * @throws IllegalArgumentException if a property or value doesn't exist for this block
     */
    public static BlockStateData parse(Material material, String states) {
        return new BlockStateData(Bukkit.createBlockData(material, states.toLowerCase()));
    }

    /** Does the actual block have every property listed in the config? */
    public boolean matches(BlockData actual) {
        return actual != null && actual.matches(state);
    }

    public BlockData getBlockData() {
        return state;
    }

    /** "[age=7]" - only the properties from the config. */
    public String getStateString() {
        String full = state.getAsString(true);   // e.g. "minecraft:wheat[age=7]"
        int bracket = full.indexOf('[');
        return bracket < 0 ? "" : full.substring(bracket);
    }

    // ---- Data interface ----

    @Override
    public boolean matches(Data d) {
        return d instanceof BlockStateData other && getStateString().equals(other.getStateString());
    }

    /** replacementblock: apply only the listed properties on top of the block's current state. */
    @Override
    public void setOn(BlockState blockState) {
        blockState.setBlockData(blockState.getBlockData().merge(state));
    }

    @Override
    public void setOn(Entity entity, Player witness) {
    }

    @Override
    public int getData() {
        return -1;
    }

    @Override
    public void setData(int d) {
    }

    @Override
    public String get(Enum<?> mat) {
        return getStateString();
    }

    @Override
    public Boolean getSheared() {
        return null;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BlockStateData data && getStateString().equals(data.getStateString());
    }

    @Override
    public int hashCode() {
        return getStateString().hashCode();
    }

    @Override
    public String toString() {
        return getStateString();
    }

    // ---- Deprecation of the old formats ----

    /**
     * Logs that "MATERIAL@oldValue" uses deprecated block data, suggesting the [property=value] form
     * when the old value is a plain number on a block with an age, level or honey level.
     */
    public static void warnDeprecated(Material material, String oldValue) {
        String suggestion = oldValue.matches("[0-9]+") ? suggestFor(material, oldValue) : null;
        Log.logWarning("'" + material + "@" + oldValue + "' uses old-style block data, which is deprecated and will be removed in a future version. "
                + (suggestion != null
                ? "Use '" + material + "@" + suggestion + "' instead."
                : "Use block states instead, like " + material + "@[property=value] (see the Data Values page on the wiki)."));
    }

    /** "[age=7]" / "[level=3]" / "[honey_level=5]" for an old numeric value, or null if the block has none of those. */
    public static String suggestFor(Material material, String number) {
        if (!material.isBlock()) return null;
        BlockData defaults = material.createBlockData();
        if (defaults instanceof Beehive) return "[honey_level=" + number + "]";
        if (defaults instanceof Ageable) return "[age=" + number + "]";
        if (defaults instanceof Levelled) return "[level=" + number + "]";
        return null;
    }
}