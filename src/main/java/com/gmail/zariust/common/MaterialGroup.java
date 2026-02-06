// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package main.java.com.gmail.zariust.common;

import main.java.com.gmail.zariust.otherdrops.Log;
import main.java.com.gmail.zariust.otherdrops.OtherDrops;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.*;

import static org.bukkit.Material.*;

public class MaterialGroup {
    private static final Map<String, MaterialGroup> lookup = new HashMap<>();

    private final ArrayList<Material> mat = new ArrayList<>();
    private final String name;

    private MaterialGroup(String name, List<Material> materials) {
        this.name = name;
        this.mat.addAll(materials);
    }

    public static MaterialGroup register(String name, Material... types) {
        return register(name, Arrays.asList(types));
    }

    public static MaterialGroup register(String name, List<Material> types) {
        MaterialGroup group = new MaterialGroup(name, types);
        lookup.put(name.toUpperCase(), group);
        return group;
    }

    private void combine(MaterialGroup... groups) {
        for (MaterialGroup group : groups) {
            this.mat.addAll(group.mat);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    // Tools
    public static final MaterialGroup ANY_SHOVEL = register("ANY_SHOVEL");
    public static final MaterialGroup ANY_AXE = register("ANY_AXE");
    public static final MaterialGroup ANY_HOE = register("ANY_HOE");
    public static final MaterialGroup ANY_PICKAXE = register("ANY_PICKAXE");
    public static final MaterialGroup ANY_SWORD = register("ANY_SWORD");
    public static final MaterialGroup ANY_BUCKET = register("ANY_BUCKET");

    // Armour
    public static final MaterialGroup ANY_HELMET = register("ANY_HELMET");
    public static final MaterialGroup ANY_CHESTPLATE = register("ANY_CHESTPLATE");
    public static final MaterialGroup ANY_LEGGINGS = register("ANY_LEGGINGS");
    public static final MaterialGroup ANY_BOOTS = register("ANY_BOOTS");

    // Wildcards
    public static final MaterialGroup ANY_SPADE = register("ANY_SPADE");
    public static final MaterialGroup ANY_TOOL = register("ANY_TOOL", FLINT_AND_STEEL, BOW, FISHING_ROD, SADDLE);
    public static final MaterialGroup ANY_WEAPON = register("ANY_WEAPON", BOW, ARROW);
    public static final MaterialGroup ANY_ARMOR = register("ANY_ARMOR");
    public static final MaterialGroup ANY_ARMOUR = register("ANY_ARMOUR");

    // Materials that have varying types
    public static final MaterialGroup ANY_SIGN = register("ANY_SIGN");
    public static final MaterialGroup ANY_LEAVES = register("ANY_LEAVES");
    public static final MaterialGroup ANY_LOGS = register("ANY_LOGS");
    public static final MaterialGroup ANY_RECORD = register("ANY_RECORD");

    public static final MaterialGroup ANY_ITEM = register("ANY_ITEM");
    public static final MaterialGroup ANY_BLOCK = register("ANY_BLOCK");
    public static final MaterialGroup ANY_OBJECT = register("ANY_OBJECT");

    static {
        register("ANY_REDSTONE_TORCH", REDSTONE_TORCH, REDSTONE_WALL_TORCH);
        register("ANY_PISTON", STICKY_PISTON, PISTON_HEAD, PISTON, MOVING_PISTON);
        register("ANY_RAIL", RAIL, POWERED_RAIL, DETECTOR_RAIL, ACTIVATOR_RAIL);
        register("ANY_PROJECTILE", FIRE_CHARGE, SNOWBALL, EGG, ARROW, FISHING_ROD, ENDER_PEARL);

        for (Material mat : values()) {
            ANY_OBJECT.mat.add(mat);
            if (mat.isBlock()) {
                ANY_BLOCK.mat.add(mat);
                if (mat.name().contains("_SIGN")) ANY_SIGN.mat.add(mat);
                if (mat.name().contains("_LEAVES")) ANY_LEAVES.mat.add(mat);
                if (mat.name().contains("_LOG")) ANY_LOGS.mat.add(mat);
            } else {
                ANY_ITEM.mat.add(mat);
                if (mat.name().contains("_SHOVEL")) ANY_SHOVEL.mat.add(mat);
                else if (mat.name().contains("_HOE")) ANY_HOE.mat.add(mat);
                else if (mat.name().contains("_AXE")) ANY_AXE.mat.add(mat);
                else if (mat.name().contains("_PICKAXE")) ANY_PICKAXE.mat.add(mat);
                else if (mat.name().contains("_SWORD")) ANY_SWORD.mat.add(mat);
                else if (mat.name().contains("_BUCKET")) ANY_BUCKET.mat.add(mat);
                else if (mat.name().contains("_HELMET")) ANY_HELMET.mat.add(mat);
                else if (mat.name().contains("_CHESTPLATE")) ANY_CHESTPLATE.mat.add(mat);
                else if (mat.name().contains("_LEGGINGS")) ANY_LEGGINGS.mat.add(mat);
                else if (mat.name().contains("_BOOTS")) ANY_BOOTS.mat.add(mat);
                else if (mat.name().contains("MUSIC_DISC")) ANY_RECORD.mat.add(mat);
            }
        }
        ANY_SPADE.combine(ANY_SHOVEL);
        ANY_TOOL.combine(ANY_SHOVEL, ANY_AXE, ANY_HOE, ANY_PICKAXE, ANY_SWORD, ANY_BUCKET);
        ANY_WEAPON.combine(ANY_SWORD);
        ANY_ARMOR.combine(ANY_HELMET, ANY_CHESTPLATE, ANY_LEGGINGS, ANY_BOOTS);
        ANY_ARMOUR.combine(ANY_ARMOR);

        registerTagMaterials();
    }

    private static void registerTagMaterials() {
        String[] registries = {"blocks", "items"};

        for (String registry : registries) {
            Iterable<Tag<Material>> tags = Bukkit.getTags(registry, Material.class);
            for (Tag<Material> tag : tags) {
                if (tag.getValues().isEmpty()) continue;

                try {
                    String name = "TAG_" + tag.getKey().toString().toUpperCase().replace("MINECRAFT:", "");
                    register(name, List.copyOf(tag.getValues()));
                } catch (Exception e) {
                    Log.logError("Failed to register tag group: " + tag.getKey(), e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<Material> materials() {
        return (List<Material>) mat.clone();
    }

    public static MaterialGroup get(String string) {
        return lookup.get(string.toUpperCase());
    }

    public static Set<String> all() {
        return lookup.keySet();
    }

    public static boolean isValid(String string) {
        return lookup.containsKey(string);
    }

    public boolean isBlock() {
        for (Material obj : mat)
            if (obj.isBlock()) return true;
        return false;
    }

    public boolean isItem() {
        for (Material obj : mat)
            if (!obj.isBlock()) return true;
        return false;
    }

    public boolean contains(Material material) {
        return mat.contains(material);
    }

    public Material getOneRandom() {
        double select = OtherDrops.rng.nextDouble() * mat.size(), cumul = 0;
        for (Material singleMat : mat) {
            cumul++;
            if (select <= cumul) {
                return singleMat;
            }
        }
        return null;
    }
}
