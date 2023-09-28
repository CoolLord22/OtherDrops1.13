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

package com.gmail.zariust.common;

import com.gmail.zariust.otherdrops.OtherDrops;
import org.bukkit.Material;

import java.util.*;

import static org.bukkit.Material.*;

public enum MaterialGroup {
    // Blocks
    ANY_REDSTONE_TORCH(REDSTONE_TORCH, REDSTONE_WALL_TORCH),
    ANY_PISTON(STICKY_PISTON, PISTON_HEAD, PISTON, MOVING_PISTON),
    ANY_LEAVES(ACACIA_LEAVES, BIRCH_LEAVES, DARK_OAK_LEAVES, JUNGLE_LEAVES, OAK_LEAVES, SPRUCE_LEAVES),
    ANY_LOGS,
    ANY_RAIL(RAIL, POWERED_RAIL, DETECTOR_RAIL, ACTIVATOR_RAIL),
    
    // Records
    ANY_RECORD(MUSIC_DISC_WARD, MUSIC_DISC_WAIT, MUSIC_DISC_STRAD, MUSIC_DISC_STAL, MUSIC_DISC_MELLOHI, MUSIC_DISC_MALL, MUSIC_DISC_FAR, MUSIC_DISC_CHIRP, MUSIC_DISC_CAT, MUSIC_DISC_BLOCKS, MUSIC_DISC_11, MUSIC_DISC_13),
   
    // Tools
    ANY_SHOVEL(WOODEN_SHOVEL, STONE_SHOVEL, GOLDEN_SHOVEL, IRON_SHOVEL, DIAMOND_SHOVEL),
    ANY_SPADE(ANY_SHOVEL),
    ANY_AXE(WOODEN_AXE, STONE_AXE, GOLDEN_AXE, IRON_AXE, DIAMOND_AXE),
    ANY_HOE(WOODEN_HOE, STONE_HOE, GOLDEN_HOE, IRON_HOE, DIAMOND_HOE),
    ANY_PICKAXE(WOODEN_PICKAXE, STONE_PICKAXE, GOLDEN_PICKAXE, IRON_PICKAXE, DIAMOND_PICKAXE),
    ANY_SWORD(WOODEN_SWORD, STONE_SWORD, GOLDEN_SWORD, IRON_SWORD, DIAMOND_SWORD), 
    ANY_BUCKET(BUCKET, LAVA_BUCKET, WATER_BUCKET, MILK_BUCKET),
    
    // Armour
    ANY_HELMET(LEATHER_HELMET, CHAINMAIL_HELMET, GOLDEN_HELMET, IRON_HELMET, DIAMOND_HELMET), 
    ANY_CHESTPLATE(LEATHER_CHESTPLATE, CHAINMAIL_CHESTPLATE, GOLDEN_CHESTPLATE, IRON_CHESTPLATE, DIAMOND_CHESTPLATE), 
    ANY_LEGGINGS(LEATHER_LEGGINGS, CHAINMAIL_LEGGINGS, GOLDEN_LEGGINGS, IRON_LEGGINGS, DIAMOND_LEGGINGS), 
    ANY_BOOTS(LEATHER_BOOTS, CHAINMAIL_BOOTS, GOLDEN_BOOTS, IRON_BOOTS, DIAMOND_BOOTS),
    
    // Wildcards
    ANY_TOOL(Arrays.asList(FLINT_AND_STEEL, BOW, FISHING_ROD, SADDLE), ANY_SHOVEL, ANY_AXE, ANY_HOE, ANY_PICKAXE, ANY_SWORD, ANY_BUCKET),
    ANY_WEAPON(Arrays.asList(BOW, ARROW), ANY_SWORD), 
    ANY_ARMOR(ANY_HELMET, ANY_CHESTPLATE, ANY_LEGGINGS, ANY_BOOTS), 
    ANY_ARMOUR(ANY_ARMOR), 
    ANY_PROJECTILE(FIRE_CHARGE, SNOWBALL, EGG, ARROW, FISHING_ROD, ENDER_PEARL),

    // Add any new ones before this line
    ANY_ITEM, ANY_BLOCK, ANY_OBJECT, ANY_SIGN;
    private static Map<String, MaterialGroup> lookup = new HashMap<String, MaterialGroup>();
    private ArrayList<Material>               mat;

    static {
        for (Material mat : Material.values()) {
            ANY_OBJECT.mat.add(mat);
            if (mat.isBlock()) {
                ANY_BLOCK.mat.add(mat);
                if(mat.name().contains("_SIGN"))
                    ANY_SIGN.mat.add(mat);
                if(mat.name().contains("_LEAVES"))
                    ANY_LEAVES.mat.add(mat);
                if(mat.name().contains("_LOG"))
                    ANY_LOGS.mat.add(mat);
            }
            else {
                ANY_ITEM.mat.add(mat);
                if(mat.name().contains("_SHOVEL"))
                    ANY_SHOVEL.mat.add(mat);
                else if(mat.name().contains("_HOE"))
                    ANY_HOE.mat.add(mat);
                else if(mat.name().contains("_AXE"))
                    ANY_AXE.mat.add(mat);
                else if(mat.name().contains("_PICKAXE"))
                    ANY_PICKAXE.mat.add(mat);
                else if(mat.name().contains("_SWORD"))
                    ANY_SWORD.mat.add(mat);
                else if(mat.name().contains("_BUCKET"))
                    ANY_BUCKET.mat.add(mat);
                else if(mat.name().contains("_HELMET"))
                    ANY_HELMET.mat.add(mat);
                else if(mat.name().contains("_CHESTPLATE"))
                    ANY_CHESTPLATE.mat.add(mat);
                else if(mat.name().contains("_LEGGINGS"))
                    ANY_LEGGINGS.mat.add(mat);
                else if(mat.name().contains("_BOOTS"))
                    ANY_BOOTS.mat.add(mat);
                else if(mat.name().contains("MUSIC_DISC"))
                    ANY_RECORD.mat.add(mat);
            }
        }
        for (MaterialGroup group : values())
            lookup.put(group.name(), group);
    }

    private void add(List<Material> materials) {
        mat.addAll(materials);
    }

    private MaterialGroup(Material... materials) {
        this();
        add(Arrays.asList(materials));
    }

    private MaterialGroup(MaterialGroup... merge) {
        this();
        for (MaterialGroup group : merge)
            add(group.mat);
    }

    private MaterialGroup(List<Material> materials, MaterialGroup... merge) {
        this(merge);
        add(materials);
    }

    private MaterialGroup() {
        mat = new ArrayList<Material>();
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
            if (obj.isBlock())
                return true;
        return false;
    }

    public boolean isItem() {
        for (Material obj : mat)
            if (!obj.isBlock())
                return true;
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
