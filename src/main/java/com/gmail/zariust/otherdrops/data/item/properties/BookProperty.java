package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import com.gmail.zariust.otherdrops.things.ODVariables;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Book text: title=…, author=…, page=… as separate entries, or joined with ":" in one entry (older configs).
 * Written and writable books. Supports & color codes; write \! for a "!" in the text.
 * As a tool: title/author must match and every listed page must be in the book.
 */
public class BookProperty implements ItemProperty {
    private enum Part {TITLE, AUTHOR, PAGE}

    private record BookPart(Part part, String text) {
    }

    private final List<BookPart> parts;

    private BookProperty(@NotNull List<BookPart> parts) {
        this.parts = List.copyOf(parts);
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (!(defaults instanceof BookMeta)) return null;
        List<BookPart> parts = new ArrayList<>();
        for (String piece : entry.split(":(?=(?i)(title|author|page)=)")) {
            String[] keyValue = piece.split("=", 2);
            if (keyValue.length < 2) return null;
            Part part = switch (keyValue[0].trim().toLowerCase()) {
                case "title" -> Part.TITLE;
                case "author" -> Part.AUTHOR;
                case "page" -> Part.PAGE;
                default -> null;
            };
            if (part == null) return null;
            parts.add(new BookPart(part, ODVariables.preParse(keyValue[1])));
        }
        return parts.isEmpty() ? null : new BookProperty(parts);
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        if (!(stack.getItemMeta() instanceof BookMeta meta)) return null;
        List<BookPart> parts = new ArrayList<>();
        if (meta.hasTitle()) parts.add(new BookPart(Part.TITLE, meta.getTitle()));
        if (meta.hasAuthor()) parts.add(new BookPart(Part.AUTHOR, meta.getAuthor()));
        for (String page : meta.getPages()) parts.add(new BookPart(Part.PAGE, page));
        return parts.isEmpty() ? null : new BookProperty(parts);
    }

    @Override
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        if (!(stack.getItemMeta() instanceof BookMeta meta)) return;
        for (BookPart part : parts) {
            switch (part.part()) {
                case TITLE -> meta.setTitle(part.text());
                case AUTHOR -> meta.setAuthor(part.text());
                case PAGE -> meta.addPage(part.text());
            }
        }
        stack.setItemMeta(meta);
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        if (stack == null || !(stack.getItemMeta() instanceof BookMeta meta)) return false;
        for (BookPart part : parts) {
            boolean ok = switch (part.part()) {
                case TITLE -> part.text().equals(meta.getTitle());
                case AUTHOR -> part.text().equals(meta.getAuthor());
                case PAGE -> meta.getPages().contains(part.text());
            };
            if (!ok) return false;
        }
        return true;
    }

    @Override
    public @NotNull String describe() {
        StringJoiner joiner = new StringJoiner("!");
        for (BookPart part : parts) {
            String text = part.text().replace('§', '&').replace("!", "\\!");
            joiner.add(part.part().name().toLowerCase() + "=" + text);
        }
        return joiner.toString();
    }

    @Override
    public @Nullable ItemProperty merge(@NotNull ItemProperty other) {
        if (!(other instanceof BookProperty o)) return null;
        List<BookPart> combined = new ArrayList<>(parts);
        combined.addAll(o.parts);
        return new BookProperty(combined);
    }
}
