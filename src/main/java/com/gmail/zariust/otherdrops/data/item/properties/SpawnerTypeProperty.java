package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** The mob a spawner item spawns once placed: SPAWNER@ZOMBIE (any mob name from the Creatures page). */
public class SpawnerTypeProperty implements ItemProperty {
    private final EntityType type;

    public SpawnerTypeProperty(@NotNull EntityType type) {
        this.type = type;
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (material != Material.SPAWNER || !(defaults instanceof BlockStateMeta)) return null;
        EntityType type = CommonEntity.getCreatureEntityType(entry);
        return type == null ? null : new SpawnerTypeProperty(type);
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        if (stack.getType() != Material.SPAWNER || !(stack.getItemMeta() instanceof BlockStateMeta meta) || !meta.hasBlockState()) return null;
        return meta.getBlockState() instanceof CreatureSpawner spawner && spawner.getSpawnedType() != null
                ? new SpawnerTypeProperty(spawner.getSpawnedType()) : null;
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        if (stack.getItemMeta() instanceof BlockStateMeta meta && meta.getBlockState() instanceof CreatureSpawner spawner) {
            spawner.setSpawnedType(type);
            meta.setBlockState(spawner);
            stack.setItemMeta(meta);
        }
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        return stack != null && stack.getItemMeta() instanceof BlockStateMeta meta && meta.hasBlockState()
                && meta.getBlockState() instanceof CreatureSpawner spawner && spawner.getSpawnedType() == type;
    }

    @Override
    public @NotNull String describe() {
        return type.name();
    }
}
