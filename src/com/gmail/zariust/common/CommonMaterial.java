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

import com.gmail.zariust.otherdrops.Log;
import org.bukkit.Material;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CommonMaterial {
    // Aliases definitions
    private static final Map<String, String> ALIASES;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("THIN_GLASS", "GLASS_PANE");
        aMap.put("WOOD_SPADE", "WOODEN_SHOVEL");
        aMap.put("WOOD_AXE", "WOODEN_AXE");
        aMap.put("WOOD_HOE", "WOODEN_HOE");
        aMap.put("WOOD_PICKAXE", "WOODEN_PICKAXE");
        aMap.put("WOOD_SWORD", "WOODEN_SWORD");
        aMap.put("GOLD_SPADE", "GOLDEN_SHOVEL");
        aMap.put("GOLDEN_SPADE", "GOLDEN_SHOVEL");
        aMap.put("GOLD_AXE", "GOLDEN_AXE");
        aMap.put("GOLD_HOE", "GOLDEN_HOE");
        aMap.put("GOLD_PICKAXE", "GOLDEN_PICKAXE");
        aMap.put("GOLD_SWORD", "GOLDEN_SWORD");
        aMap.put("LEATHER_HELM", "LEATHER_HELMET");
        aMap.put("IRON_HELM", "IRON_HELMET");
        aMap.put("GOLD_HELM", "GOLDEN_HELMET");
        aMap.put("DIAMOND_HELM", "DIAMOND_HELMET");
        aMap.put("HANDS", "AIR");
        aMap.put("HAND", "AIR");
        aMap.put("NOTHING", "AIR");

        aMap.put("YELLOW_FLOWER", "DANDELION");
        aMap.put("ROSE", "ROSE_BUSH");
        aMap.put("RED_FLOWER", "ROSE_BUSH");

        aMap.put("MOSS_STONE", "MOSSY_COBBLESTONE");
        aMap.put("MOSSY_COBBLE", "MOSSY_COBBLESTONE");
        aMap.put("SULPHUR", "GUNPOWDER");
        aMap.put("SULFUR", "GUNPOWDER");
        aMap.put("WORKBENCH", "CRAFTING_TABLE");
        aMap.put("SOIL", "FARMLAND");
        aMap.put("SEEDS", "WHEAT_SEEDS");
        aMap.put("VINES", "VINE");
        aMap.put("SMOOTH_BRICK", "STONE_BRICKS");
        aMap.put("TRACKS", "RAIL");
        aMap.put("TRACK", "RAIL");
        aMap.put("RAILS", "RAIL");
        aMap.put("ZOMBIE_FLESH", "ROTTEN_FLESH");
        aMap.put("SPECKLED_MELON", "GLISTERING_MELON_SLICE");
        aMap.put("melonslice", "MELON_SLICE");
        aMap.put("uncookedbeef", "BEEF");
        aMap.put("steak", "COOKED_BEEF");
        aMap.put("netherwartseeds", "NETHER_WART");
        aMap.put("netherstalk", "NETHER_WART");
        aMap.put("melonseed", "melon_seeds");
        aMap.put("pumpkinseed", "pumpkin_seeds");
        aMap.put("redstonerepeater", "REPEATER");
        aMap.put("diode", "REPEATER");
        aMap.put("watch", "clock");
        aMap.put("pork", "PORKCHOP");
        aMap.put("cookedpork", "COOKED_PORKCHOP");
        aMap.put("cookedporkchop", "COOKED_PORKCHOP");
        aMap.put("grilledporkchop", "COOKED_PORKCHOP");
        aMap.put("rabbit_meat", "rabbit");
        aMap.put("raw_rabbit", "rabbit");

        aMap.put("goldpants", "golden_leggings");
        aMap.put("goldvest", "golden_chestplate");
        aMap.put("goldchest", "golden_chestplate");

        aMap.put("mushroomsoup", "mushroom_stew");
        aMap.put("flintandtinder", "flint_and_steel");
        aMap.put("ironfence", "iron_bars");
        aMap.put("glowstoneblock", "glowstone");
        aMap.put("netherrock", "netherrack");
        aMap.put("cacti", "cactus");
        aMap.put("web", "cobweb");
        aMap.put("poweredtracks", "powered_rail");
        aMap.put("powerrail", "powered_rail");
        aMap.put("detectortracks", "detector_rail");

        aMap.put("leathercap", "leather_helmet");
        aMap.put("leathertunic", "leather_chestplate");
        aMap.put("leathervest", "leather_chestplate");
        aMap.put("leatherchest", "leather_chestplate");
        aMap.put("leatherpants", "leather_leggings");

        aMap.put("chainmailvest", "chainmail_chestplate");
        aMap.put("chainmailchest", "chainmail_chestplate");
        aMap.put("chainmailpants", "chainmail_leggings");

        aMap.put("ironvest", "iron_chestplate");
        aMap.put("ironchest", "iron_chestplate");
        aMap.put("ironpants", "iron_leggings");

        aMap.put("diamondvest", "diamond_chestplate");
        aMap.put("diamondchest", "diamond_chestplate");
        aMap.put("diamondpants", "diamond_leggings");

        aMap.put("diamondspade", "diamond_shovel");
        aMap.put("goldspade", "goldspadeshovel");
        aMap.put("ironspade", "ironshovel");
        aMap.put("stonespade", "stoneshovel");
        aMap.put("woodspade", "woodshovel");

        aMap.put("goldapple", "golden_apple");
        aMap.put("redapple", "apple");
        aMap.put("sticks", "stick");

        aMap.put("longgrass", "tall_grass");
        aMap.put("wildgrass", "tall_grass");

        aMap.put("lapislazuliore", "lapis_ore");
        aMap.put("lapislazuliblock", "lapis_block");
        aMap.put("pistonstickybase", "sticky_piston");
        aMap.put("pistonbase", "piston");
        aMap.put("bricks", "brick");
        aMap.put("mycel", "mycelium");
        aMap.put("waterlily", "lily_pad");
        aMap.put("netherfence", "nether_brick_fence");

        aMap.put("smoothbrick", "stone_bricks");
        aMap.put("smoothstairs", "stone_brick_stairs");

        aMap.put("enderportal", "end_portal");
        aMap.put("enderportalframe", "end_portal_frame");
        aMap.put("enderstone", "end_stone");

        aMap.put("rawporkchop", "porkchop");
        aMap.put("netherwarts", "nether_wart");

        aMap.put("cauldron_block", "cauldron");
        aMap.put("brewing_stand_block", "brewing_stand");

        aMap.put("bucket of milk", "milk_bucket");
        
        aMap.put("fireball", "fire_charge");

        // Records
        aMap.put("13disc", "MUSIC_DISC_13");
        aMap.put("goldrecord", "MUSIC_DISC_13");
        aMap.put("catdisc", "MUSIC_DISC_CAT");
        aMap.put("greenrecord", "MUSIC_DISC_CAT");
        aMap.put("blocksdisc", "MUSIC_DISC_BLOCKS");
        aMap.put("record3", "MUSIC_DISC_BLOCKS");
        aMap.put("chirpdisc", "MUSIC_DISC_CHIRP");
        aMap.put("record4", "MUSIC_DISC_CHIRP");
        aMap.put("fardisc", "MUSIC_DISC_FAR");
        aMap.put("record5", "MUSIC_DISC_FAR");
        aMap.put("malldisc", "MUSIC_DISC_MALL");
        aMap.put("record6", "MUSIC_DISC_MALL");
        aMap.put("mellohidisc", "MUSIC_DISC_MELLOHI");
        aMap.put("record7", "MUSIC_DISC_MELLOHI");
        aMap.put("staldisc", "MUSIC_DISC_STAL");
        aMap.put("record8", "MUSIC_DISC_STAL");
        aMap.put("straddisc", "MUSIC_DISC_STRAD");
        aMap.put("record9", "MUSIC_DISC_STRAD");
        aMap.put("warddisc", "MUSIC_DISC_WARD");
        aMap.put("record10", "MUSIC_DISC_WARD");
        aMap.put("11disc", "MUSIC_DISC_11");
        aMap.put("record11", "MUSIC_DISC_11");
        aMap.put("waitdisc", "MUSIC_DISC_WAIT");
        aMap.put("record12", "MUSIC_DISC_WAIT");

        aMap.put("command", "command_block");

        // 1.6.1 mats
        aMap.put("leash", "lead");
        aMap.put("haybale", "hay_block");
        aMap.put("iron_horse_armour", "iron_horse_armor");
        aMap.put("gold_horse_armour", "gold_horse_armor");
        aMap.put("diamond_horse_armour", "diamond_horse_armor");
        aMap.put("ironbarding", "iron_horse_armor");
        aMap.put("goldbarding", "gold_horse_armor");
        aMap.put("diamondbarding", "diamond_horse_armor");
        aMap.put("blockofcoal", "coal_block");

        ALIASES = Collections.unmodifiableMap(aMap);
    }

	public static Material matchMaterial(String mat) {
        // Aliases defined here override those in Material; the only example
        // here is WOODEN_DOOR
        // You can remove it if you prefer not to break the occasional config
        // file.
        // (I doubt many people assign drops to wooden doors, though, and
        // including the BLOCK makes it less confusing.)

        // remove any trailing data (eg. from tool [item]/[quantity])
        String[] split = mat.split("/");
        mat = split[0];
        
        if (mat.matches("[0-9]+")) {
            Log.logWarning("Error while parsing: " + mat + ". Support for numerical IDs has been dropped!");
        }
        // CommonMaterial material = enumValue(CommonMaterial.class, mat);
        mat = mat.toLowerCase().replaceAll("[\\s-_]", "");

        for (String loopAlias : ALIASES.keySet()) {
            if (mat.equalsIgnoreCase(loopAlias.toLowerCase().replaceAll("[\\s-_]", "")))
                mat = ALIASES.get(loopAlias).toLowerCase().replaceAll("[\\s-_]", "");
        }

        Material matchedMat = null;
        for (Material loopMat : Material.values()) {
            if (mat.equalsIgnoreCase(loopMat.name().toLowerCase().replaceAll("[\\s-_]", "")))
                matchedMat = loopMat;
        }

        if (matchedMat == null) {
            Material defaultMat = Material.getMaterial(mat);
            if (defaultMat == null) {
                if (!(mat.equalsIgnoreCase("default"))) {
                    Log.logInfo("Error: unknown material (" + mat + ").", Verbosity.LOW);
                }
            }
        }
        return matchedMat;
    }

    public static Integer parseBlockOrItemData(Material mat, String state)
            throws IllegalArgumentException {
    	
        Log.logInfo("Checking block data for " + mat.toString() + "@" + state, Verbosity.HIGH);
        
        state = state.toUpperCase();
        if (state.equalsIgnoreCase("this"))
            return -1;
        return null;
    }

    public static String getBlockOrItemData(Material mat, int data) {
        try {
            if (data > 0)
                return Integer.toString(data);
            return "";
        } catch (NullPointerException ex) {
            Log.logWarning(
                    "CommonMaterial.getBlockOrItemData() failed. Material: " + mat.toString() + ", Data: " + data, Verbosity.NORMAL);
            return "";
        }
    }

    public static String substituteAlias(String drop) {
        Map<String, String> a2Map = new HashMap<String, String>();

        // note: aliases (on left) need to be uppercase with no spaces, dashes or underscores
        a2Map.put("ANYSHOVEL", "ANY_SPADE");

        String tmpDrop = drop.toUpperCase().replaceAll("[ _-]", "");
        for (String alias : a2Map.keySet()) {
            if (tmpDrop.toUpperCase().replaceAll("[ _-]", "").matches(alias + ".*")) {
                String[] nameSplit = tmpDrop.split("~", 2);
                tmpDrop = nameSplit[0].replaceAll("@", "!");
                String[] nameSplit2 = tmpDrop.split("!", 2);
                tmpDrop = nameSplit2[0];
                tmpDrop = tmpDrop.toUpperCase().replaceAll("[ _-]", "").replaceAll("(?i)" + alias, a2Map.get(alias));
                if (nameSplit.length > 1) tmpDrop += "~"+nameSplit[1];
                if (nameSplit2.length > 1) tmpDrop += "!"+nameSplit2[1];
                return tmpDrop; // we only want to replace the first found result,
                             // so return
            }
        }

        return drop;
    }

    public static boolean fuzzyMatchString(String one, String two) {

        if (one.toLowerCase().replaceAll("[\\s-_]", "").equals(two.toLowerCase().replaceAll("[\\s-_]", "")))
            return true;
        return false;
    }
}
