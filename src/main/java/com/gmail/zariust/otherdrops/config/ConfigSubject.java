package com.gmail.zariust.otherdrops.config;

import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.common.MaterialGroup;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.item.ODItem;
import com.gmail.zariust.otherdrops.subject.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A subject definition from config: IDENTIFIER@[entry!entry...]~[Name;lore;lore]/N
 * This class only knows the syntax. {@link #getTarget()}, {@link #getAgent()} and {@link #getODItem()} decide what
 * kind of thing it is and hand it to that thing's parse method; the objects they build never see config text.
 */
public class ConfigSubject {
    private static final Pattern QUANTITY_SUFFIX = Pattern.compile("/([0-9]+)$");

    /** Raw string data read in from the input string ~ */
    private final String identifier;        // part before '@'
    private final String rawData;           // part from '@' until '~'
    private final String displayText;       // part after '~'
    private final int quantity;             // "/N" suffix (tools); 1 if none
    private final List<String> dataTokens;  // rawDataString as CommonDataTokens

    public ConfigSubject(String identifier, String rawData, List<String> dataTokens, @Nullable String displayText, int quantity) {
        this.identifier = identifier.toUpperCase();
        this.rawData = rawData;
        this.dataTokens = List.copyOf(dataTokens);
        this.displayText = displayText;
        this.quantity = quantity;
    }

    public static @NotNull ConfigSubject parseSubject(@NotNull String input) {
        String text = CommonMaterial.substituteAlias(input.trim());

        String displayText = null;
        int tilde = text.indexOf('~');
        if (tilde >= 0) {
            displayText = text.substring(tilde + 1).replace("slashCharPlaceholder", "/");
            text = text.substring(0, tilde);
        }

        int quantity = 1;
        Matcher suffix = QUANTITY_SUFFIX.matcher(text);
        if (suffix.find()) {
            quantity = Integer.parseInt(suffix.group(1));
            text = text.substring(0, suffix.start());
        }

        String[] split = text.split("@", 2);
        String identifier = split[0].replace("slashCharPlaceholder", "/");
        String rawData = split.length > 1 ? split[1].replace("slashCharPlaceholder", "/") : "";
        return new ConfigSubject(identifier, rawData, ConfigDataTokens.split(rawData.replace(';', '!')), displayText, quantity);
    }

    /** The same subject with this data when it has none (drops' "data:" option); "0" means none. */
    public @NotNull ConfigSubject withDefaultData(@Nullable String defaultData) {
        if (!rawData.isEmpty() || defaultData == null || defaultData.isEmpty() || defaultData.equals("0")) return this;
        return new ConfigSubject(identifier, defaultData, ConfigDataTokens.split(defaultData.replace(';', '!')), displayText, quantity);
    }

    // ---- Accessors ----
    public @NotNull String getIdentifier() {
        return identifier;
    }
    public @NotNull String getRawData() {
        return rawData;
    }
    public @NotNull List<String> getDataTokens() {
        return dataTokens;
    }
    public @Nullable String getDisplayText() {
        return displayText;
    }
    public @Nullable String getDisplayName() {
        if (displayText == null) return null;
        return ChatColor.translateAlternateColorCodes('&', displayText.split(";", 2)[0]);
    }
    public int getQuantity() {
        return quantity;
    }
    public @NotNull String describe() {
        return identifier + (rawData.isBlank() ? "" : "@" + rawData) + (displayText == null ? "" : "~" + displayText);
    }

    @Override
    public String toString() {
        return describe() + (quantity == 1 ? "" : "/" + quantity);
    }

    // ---- What kind of thing is it? ----
    public @Nullable Agent getAgent() {
        // Agent can be one of the following
        // - One of the Material synonyms NOTHING and DYE
        // - A MaterialGroup constant
        // - One of the special wildcards ANY, ANY_CREATURE, ANY_DAMAGE
        // - One of the keywords PLAYER or PLAYERGROUP
        // - A DamageCause constant prefixed by DAMAGE_
        // - DAMAGE_FIRE_TICK and DAMAGE_CUSTOM are valid but not allowed
        // - DAMAGE_WATER is invalid but allowed, and stored as CUSTOM
        // - A MythicMob
        // - A EntityType constant prefixed by CREATURE_
        // - A projectile; ie a Material constant prefixed by PROJECTILE_
        // - An explosion
        // - A tool; ie, a Material constant
        if (MaterialGroup.isValid(identifier) || identifier.startsWith("ANY") || identifier.equals("ALL")) return AnySubject.parseAgent(identifier);
        if (identifier.equals("PLAYER")) return PlayerSubject.parse(rawData);
        if (identifier.equals("PLAYERGROUP")) return new GroupSubject(rawData);
        if (identifier.startsWith("DAMAGE_")) return EnvironmentAgent.parse(identifier, rawData);
        if (identifier.startsWith("MYTHIC_MOB")) return MythicMobSubject.parse(rawData);

        LivingSubject creatureSubject = CreatureSubject.parse(identifier, rawData, displayText);
        if (creatureSubject != null) return creatureSubject;
        if (identifier.startsWith("PROJECTILE")) return ProjectileAgent.parse(identifier, rawData);
        if (identifier.startsWith("EXPLOSION")) return ExplosionAgent.parse(identifier, rawData);
        return ToolAgent.parse(this);
    }
    public @Nullable Target getTarget() {
        // Target name is one of the following:
        // - One of the keywords PLAYER or PLAYERGROUP
        // - A MaterialGroup constant containing blocks
        // - A Material constant that is a block, painting, or vehicle
        // - Vehicle starting with VEHICLE (note: BOAT, MINECART, etc. can only be vehicles in a target so process accordingly)
        // - A MythicMob
        // - A EntityType constant prefixed by CREATURE_
        if (identifier.equals("PLAYER")) return PlayerSubject.parse(rawData);
        if (identifier.equals("PLAYERGROUP")) return new GroupSubject(rawData);
        if (identifier.equals("SPECIAL_LEAFDECAY")) {
            Log.logWarning("SPECIAL_LEAFDECAY is deprecated; use ANY_LEAVES (or TAG_LEAVES) with trigger: LEAF_DECAY.");
            return AnySubject.parseTarget("ANY_LEAVES"); // for compatibility
        }
        if (MaterialGroup.isValid(identifier) || identifier.startsWith("ANY") || identifier.equals("ALL")) return AnySubject.parseTarget(identifier);
        if (identifier.startsWith("VEHICLE") || identifier.matches("BOAT|MINECART|BOAT_SPRUCE|BOAT_JUNGLE|BOAT_BIRCH|BOAT_ACACIA|BOAT_DARK_OAk"))
            return VehicleTarget.parse(Material.getMaterial(identifier.replace("VEHICLE_", "")), rawData);
        if (identifier.startsWith("MYTHIC_MOB")) return MythicMobSubject.parse(rawData);

        LivingSubject creatureSubject = CreatureSubject.parse(identifier, rawData, displayText);
        if (creatureSubject != null) return creatureSubject;
        return BlockTarget.parse(identifier, rawData, displayText);
    }
    public @Nullable ODItem getODItem() {
        return ODItem.parse(this);
    }
}
