package com.gmail.zariust.otherdrops.data.item.properties;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.drop.DropType.DropFlags;
import com.gmail.zariust.otherdrops.subject.Target;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Which map a filled map shows: map=ID (the number in the map's tooltip / map_ID.dat). The map color is a ColorProperty. */
public class MapProperty implements ItemProperty {
    private final int mapId;

    public MapProperty(int mapId) {
        this.mapId = mapId;
    }

    public static @Nullable ItemProperty parse(@NotNull Material material, @Nullable ItemMeta defaults, @NotNull String entry) {
        if (!(defaults instanceof MapMeta) || !entry.toLowerCase().matches("map=[0-9]+")) return null;
        return new MapProperty(Integer.parseInt(entry.substring("map=".length())));
    }

    public static @Nullable ItemProperty read(@NotNull ItemStack stack) {
        return stack.getItemMeta() instanceof MapMeta meta && meta.hasMapView() && meta.getMapView() != null
                ? new MapProperty(meta.getMapView().getId()) : null;
    }

    @Override
    @SuppressWarnings("deprecation") // Bukkit.getMap(int) is the only way to look a map up by its number
    public void applyTo(@NotNull ItemStack stack, @Nullable Target source, @Nullable DropFlags flags) {
        if (!(stack.getItemMeta() instanceof MapMeta meta)) return;
        MapView view = Bukkit.getMap(mapId);
        if (view == null) {
            Log.logWarning("Map " + mapId + " doesn't exist on this server; the map is left blank.");
            return;
        }
        meta.setMapView(view);
        stack.setItemMeta(meta);
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        return stack != null && stack.getItemMeta() instanceof MapMeta meta && meta.hasMapView()
                && meta.getMapView() != null && meta.getMapView().getId() == mapId;
    }

    @Override
    public @NotNull String describe() {
        return "map=" + mapId;
    }
}
