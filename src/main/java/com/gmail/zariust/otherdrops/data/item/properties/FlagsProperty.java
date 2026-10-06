package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;
import java.util.StringJoiner;

/** Item flags: HIDE_ENCHANTS, HIDE_ATTRIBUTES… (underscores optional). As a tool, the item needs at least these. */
public class FlagsProperty implements ItemProperty {
    private final Set<ItemFlag> flags;

    public FlagsProperty(@NotNull Set<ItemFlag> flags) {
        this.flags = EnumSet.copyOf(flags);
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        String wanted = entry.replace("_", "");
        for (ItemFlag flag : ItemFlag.values())
            if (flag.name().replace("_", "").equalsIgnoreCase(wanted)) return new FlagsProperty(EnumSet.of(flag));
        return null;
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || meta.getItemFlags().isEmpty()) return null;
        return new FlagsProperty(meta.getItemFlags());
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags dropFlags) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        meta.addItemFlags(flags.toArray(new ItemFlag[0]));
        stack.setItemMeta(meta);
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        ItemMeta meta = stack == null ? null : stack.getItemMeta();
        return meta != null && meta.getItemFlags().containsAll(flags);
    }

    @Override
    public @NotNull String describe() {
        StringJoiner joiner = new StringJoiner("!");
        for (ItemFlag flag : flags) joiner.add(flag.name());
        return joiner.toString();
    }

    @Override
    public @Nullable ItemProperty merge(@NotNull ItemProperty other) {
        if (!(other instanceof FlagsProperty o)) return null;
        Set<ItemFlag> combined = EnumSet.copyOf(flags);
        combined.addAll(o.flags);
        return new FlagsProperty(combined);
    }
}
