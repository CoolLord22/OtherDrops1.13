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

import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.*;

import java.util.*;

//import static org.bukkit.Material.*;

public final class CommonEntity {

    /**
     * Return a EntityType if the given string is a valid type or alias for a
     * creature. Ignore non creature entities unless prefixed with CREATURE_ or
     * ENTITY_ We could use EntityType.fromName(string) but it would not be case
     * (or dash) insensitive.
     * 
     * @param name
     *            - spaces, dashes and underscores are ignored, case insensitive
     * @return EntityType or null if no valid type
     */
    public static EntityType getCreatureEntityType(String name) {
        if (name == null || name.isEmpty())
            return null;
        List<String> conflictMobs = new ArrayList<String>();
        conflictMobs.add("chicken");
        conflictMobs.add("cod");
        conflictMobs.add("salmon");
        conflictMobs.add("pufferfish");
        conflictMobs.add("tropicalfish");
        
        name = name.split("@")[0].toLowerCase(); // remove data value, if any,
                                                 // and make **lowercase** (keep
                                                 // in mind below)
        name = name.replaceAll("[\\s-_]", ""); // remove spaces, dashes &
                                               // underscores
        if(conflictMobs.contains(name.toLowerCase()))
        	return null;
        
        if(name.equalsIgnoreCase("tntprimed"))
        	return EntityType.PRIMED_TNT;
        
        
        boolean isEntity = false;
        if (name.matches("^entity.*"))
            isEntity = true;

        name = name.replaceAll("^creature", "");
        name = name.replaceAll("^entity", "");

        // Log.logInfo("Checking creature '"+name+"' (original name: '"+originalName+"')",
        // Verbosity.HIGH);

        // Creature aliases - format: (<aliasvalue>, <bukkitmobname>) - must be
        // lowercase
        Map<String, String> replacer = new HashMap<String, String>();
        replacer.put("mooshroom", "mushroomcow");
        replacer.put("endermen", "enderman");
        replacer.put("zombiepig", "pigzombie");
        replacer.put("pigman", "pigzombie");
        replacer.put("zombiepigman", "pigzombie");
        replacer.put("dog", "wolf");
        replacer.put("snowgolem", "snowman");
        replacer.put("lavaslime", "magmacube");
        replacer.put("magmaslime", "magmacube");

        if(replacer.containsKey(name.toLowerCase()))
        	name = replacer.get(name.toLowerCase());
        
        Set<EntityType> possibleMatches = new HashSet<EntityType>();

        for (EntityType creature : EntityType.values()) {
            String compareShortcut = ";" + (creature.toString().toLowerCase().replaceAll("[\\s-_]",""));
            if (compareShortcut.matches(name + ".*"))
                possibleMatches.add(creature);
            if (name.equalsIgnoreCase(creature.name().toLowerCase().replaceAll("[\\s-_]", "")))
                if (creature.isAlive() || isEntity) {
                    return creature;
                }
        }
        if (possibleMatches.size() == 1)
            return (EntityType) possibleMatches.toArray()[0];

        return null;
    }

    public static Material getVehicleType(Entity e) {
        if (e instanceof Boat) {
            TreeSpecies boat = ((Boat) e).getWoodType();
            if(boat.equals(TreeSpecies.ACACIA))
            	return Material.ACACIA_BOAT;
            if(boat.equals(TreeSpecies.BIRCH))
                return Material.BIRCH_BOAT;
            if(boat.equals(TreeSpecies.DARK_OAK))
                return Material.DARK_OAK_BOAT;
            if(boat.equals(TreeSpecies.GENERIC))
                return Material.OAK_BOAT;
            if(boat.equals(TreeSpecies.JUNGLE))
                return Material.JUNGLE_BOAT;
            if(boat.equals(TreeSpecies.REDWOOD))
                return Material.SPRUCE_BOAT;
        }
        if (e instanceof Minecart)
            return Material.MINECART;
        if (e instanceof CommandMinecart)
            return Material.COMMAND_BLOCK_MINECART;
        if (e instanceof ExplosiveMinecart)
        	return Material.TNT_MINECART;
        if (e instanceof HopperMinecart)
        	return Material.HOPPER_MINECART;
        if (e instanceof PoweredMinecart)
        	return Material.FURNACE_MINECART;
        if (e instanceof StorageMinecart)
        	return Material.CHEST_MINECART;
        return null;
    }

    public static Material getProjectileType(Entity e) {
        if (!(e instanceof Projectile)) return null;

        // Arrow, Egg, EnderPearl, Fireball, Fish, LargeFireball, SmallFireball, Snowball, ThrownExpBottle, ThrownPotion, WitherSkull, SpectralArrow, and TippedArrow
        if (e instanceof Arrow)
            return Material.ARROW;
        if (e instanceof DragonFireball)
            return Material.FIRE_CHARGE;
        if (e instanceof Egg)
            return Material.EGG;
        if (e instanceof EnderPearl)
            return Material.ENDER_PEARL;
        if (e instanceof Fireball)
            return Material.FIRE_CHARGE;
        if (e instanceof FishHook)
            return Material.FISHING_ROD;
        if (e instanceof LargeFireball)
            return Material.FIRE_CHARGE;
        if (e instanceof LingeringPotion)
            return Material.POTION;
        if (e instanceof SmallFireball)
            return Material.FIRE_CHARGE;
        if (e instanceof Snowball)
            return Material.SNOWBALL;
        if (e instanceof SpectralArrow)
            return Material.SPECTRAL_ARROW;
        if (e instanceof SplashPotion)
            return Material.POTION;
        if (e instanceof ThrownExpBottle)
            return Material.EXPERIENCE_BOTTLE;
        if (e instanceof ThrownPotion)
            return Material.POTION;
        if (e instanceof TippedArrow)
            return Material.TIPPED_ARROW;
        if (e instanceof Trident)
            return Material.TRIDENT;
        if (e instanceof WitherSkull)
            return Material.WITHER_SKELETON_SKULL;
        return null;
    }

    public static Material getExplosiveType(Entity e) {
        if (e instanceof Fireball)
            return Material.FIRE_CHARGE;
        if (e instanceof TNTPrimed)
            return Material.TNT;
        return null;
    }
}
