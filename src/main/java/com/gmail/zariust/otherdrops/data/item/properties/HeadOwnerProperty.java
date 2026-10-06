package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

/**
 * Whose head a PLAYER_HEAD shows:
 * <ul>
 *   <li>a player name: PLAYER_HEAD@Notch</li>
 *   <li>%v (or THIS): the victim; a player's own head, or Mojang's MHF_ head for mobs that have one</li>
 *   <li>a skin texture URL: PLAYER_HEAD@https://textures.minecraft.net/texture/…</li>
 * </ul>
 * Names use setOwningPlayer; texture URLs use the PlayerProfile API (1.18.1+; skipped with a warning on 1.17).
 * Accepts any text, so it's tried last.
 */
public class HeadOwnerProperty implements ItemProperty {
    /** Mobs with an official Mojang "MHF_" head account. */
    private static final Map<String, String> MHF_HEADS = Map.ofEntries(
            Map.entry("BLAZE", "MHF_Blaze"), Map.entry("CHICKEN", "MHF_Chicken"), Map.entry("COW", "MHF_Cow"),
            Map.entry("CREEPER", "MHF_Creeper"), Map.entry("ENDERMAN", "MHF_Enderman"), Map.entry("GHAST", "MHF_Ghast"),
            Map.entry("OCELOT", "MHF_Ocelot"), Map.entry("PIG", "MHF_Pig"), Map.entry("SHEEP", "MHF_Sheep"),
            Map.entry("SKELETON", "MHF_Skeleton"), Map.entry("MUSHROOM_COW", "MHF_MushroomCow"),
            Map.entry("MOOSHROOM", "MHF_MushroomCow"), Map.entry("SLIME", "MHF_Slime"), Map.entry("SPIDER", "MHF_Spider"),
            Map.entry("SQUID", "MHF_Squid"), Map.entry("VILLAGER", "MHF_Villager"), Map.entry("ZOMBIE", "MHF_Zombie"),
            Map.entry("CAVE_SPIDER", "MHF_CaveSpider"), Map.entry("ZOMBIFIED_PIGLIN", "MHF_PigZombie"),
            Map.entry("IRON_GOLEM", "MHF_Golem"), Map.entry("MAGMA_CUBE", "MHF_LavaSlime"),
            Map.entry("WITHER_SKELETON", "MHF_WSkeleton"));

    private final String owner;

    public HeadOwnerProperty(@NotNull String owner) {
        this.owner = owner;
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (material != Material.PLAYER_HEAD || !(defaults instanceof SkullMeta)) return null;
        return new HeadOwnerProperty(entry);
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        if (!(stack.getItemMeta() instanceof SkullMeta meta)) return null;
        OfflinePlayer owning = meta.getOwningPlayer();
        if (owning != null && owning.getName() != null) return new HeadOwnerProperty(owning.getName());
        try {
            PlayerProfile profile = meta.getOwnerProfile();
            URL skin = profile == null ? null : profile.getTextures().getSkin();
            if (skin != null) return new HeadOwnerProperty(skin.toString());
        } catch (Throwable ignored) { // PlayerProfile unavailable (1.17)
        }
        return null;
    }

    private boolean isVictim() {
        return owner.equalsIgnoreCase("%v") || owner.equalsIgnoreCase("THIS");
    }

    private boolean isTextureUrl() {
        return owner.startsWith("http://") || owner.startsWith("https://");
    }

    /** The real owner for this drop: %v / THIS resolved from the event's target. Null if it can't be resolved. */
    private @Nullable String resolve(@Nullable Target source) {
        if (!isVictim()) return owner;
        if (source instanceof PlayerSubject player && player.getPlayer() != null) return player.getPlayer().getName();
        if (source instanceof CreatureSubject) {
            EntityType type = CommonEntity.getCreatureEntityType(source.toString().split("@")[0]);
            if (type != null) return MHF_HEADS.getOrDefault(type.name(), "MHF_" + type.name());
        }
        return null;
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        if (!(stack.getItemMeta() instanceof SkullMeta meta)) return;
        String resolved = resolve(source);
        if (resolved == null) return;
        try {
            if (isTextureUrl()) {
                PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
                profile.getTextures().setSkin(URI.create(resolved).toURL());
                meta.setOwnerProfile(profile);
            } else {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(resolved));
            }
            stack.setItemMeta(meta);
        } catch (Throwable e) { // bad URL, or PlayerProfile unavailable (1.17)
            Log.logWarning("Couldn't set the head owner to '" + resolved + "': " + e.getMessage());
        }
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        if (stack == null || !(stack.getItemMeta() instanceof SkullMeta meta)) return false;
        if (isVictim()) return true; // depends on the event, can't be checked against a fixed item
        if (isTextureUrl()) {
            try {
                PlayerProfile profile = meta.getOwnerProfile();
                return profile != null && profile.getTextures().getSkin() != null
                        && owner.equals(profile.getTextures().getSkin().toString());
            } catch (Throwable e) {
                return false;
            }
        }
        OfflinePlayer owning = meta.getOwningPlayer();
        return owning != null && owner.equalsIgnoreCase(owning.getName());
    }

    @Override
    public @NotNull String describe() {
        return owner;
    }
}
