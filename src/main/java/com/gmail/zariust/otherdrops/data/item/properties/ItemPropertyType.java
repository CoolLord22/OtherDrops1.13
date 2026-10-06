package com.gmail.zariust.otherdrops.data.item.properties;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** One kind of trait that can appear after "@": how to parse it from config, and how to read it off a real item. */
public record ItemPropertyType(@NotNull String name, @NotNull Parser parser, @NotNull Reader reader) {

    @FunctionalInterface
    public interface Parser {
        /** @param defaults the material's default ItemMeta (null for air), to check which kind of item this is */
        @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry);
    }

    @FunctionalInterface
    public interface Reader {
        @Nullable ItemProperty read(@NotNull ItemStack stack);
    }
}
