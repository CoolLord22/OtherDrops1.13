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

package com.gmail.zariust.otherdrops.options;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.*;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

import static com.gmail.zariust.common.Verbosity.HIGHEST;

/**
 * Represents a boolean flag which a drop can either satisfy or not satisfy.
 */
public abstract class Flag implements Comparable<Flag> {
    /**
     * Indicates that no other drop can accompany this drop.
     */
    public final static Flag UNIQUE = new Flag("UNIQUE") {
    	@Override
        public void matches(OccurredEvent event, boolean state, final FlagState result) {
    		if (state) {
    			Log.logInfo("UNIQUE flag found...",Verbosity.HIGHEST); 
    			result.dropThis = true;
    			result.continueDropping = false;
    			}
    		}
    	};

    public final static Flag WORLDGUARD_BUILD_PERMISSION = new Flag("WORLDGUARD_BUILD_PERMISSION") {
    	@Override
    	public void matches(OccurredEvent event, boolean state, final FlagState result) {
    		if (Dependencies.hasWorldGuard()) {
    			Player player = null;
    			if (event.getTool() instanceof PlayerSubject) {
    				player = ((PlayerSubject) event.getTool()).getPlayer();
                }
    			if (player != null) {
    				if (Dependencies.getWorldGuard().createProtectionQuery().testBlockPlace(player, event.getLocation(), event.getLocation().getBlock().getType())) {
    					Log.logInfo("Worldguard build permission allowed.", HIGHEST);
    					result.dropThis = true;
    					} else {
    						Log.logInfo("Worldguard build permission failed.", HIGHEST);
    						result.dropThis = false;
    						}
    				}
    			}
    		}
    	};

    public final static Flag IN_MOB_ARENA = new Flag("IN_MOB_ARENA") {
    	@Override
    	public void matches(OccurredEvent event, boolean state, final FlagState result) {
    		if (state == false) {
    			result.dropThis = true;
    			result.continueDropping = true;
    			} else {
    				result.continueDropping = true;
    				if (!Dependencies.hasMobArena()) {
    					Log.logInfo("Checking IN_MOB_ARENA flag.  Mobarena not loaded so drop ignored.", Verbosity.HIGH);
    					result.dropThis = false;
    					} else {
    						if (Dependencies.getMobArenaHandler().inRunningRegion(event.getLocation())) {
    							Log.logInfo("Checking IN_MOB_ARENA flag. In arena = true, drop allowed.", Verbosity.HIGH);
    							result.dropThis = true;
    							} else {
    								Log.logInfo("Checking IN_MOB_ARENA flag. In arena = false, drop ignored.", Verbosity.HIGH);
    								result.dropThis = false;
    								}
    						}
    				}
    		}
    	};

    public final static Flag TOWNY_BUILD_PERMISSION = new Flag("TOWNY_BUILD_PERMISSION") {
        @Override
        public void matches(OccurredEvent event, boolean state, final FlagState result) {
            if (Dependencies.hasTowny()) {
                Player player = null;
                if (event.getTool() instanceof PlayerSubject) {
                    player = ((PlayerSubject) event.getTool()).getPlayer();
                }
                if (player != null) {
                    if(PlayerCacheUtil.getCachePermission(player, event.getLocation(), event.getLocation().getBlock().getType(), TownyPermission.ActionType.BUILD)) {
                        Log.logInfo("Towny build permission allowed.", HIGHEST);
                        result.dropThis = true;
                    } else {
                        Log.logInfo("Towny build permission failed.", HIGHEST);
                        result.dropThis = false;
                    }
                }
            }
        }
    };

    public final static Flag GRIEFPREVENTION_BUILD_PERMISSION = new Flag("GRIEFPREVENTION_BUILD_PERMISSION") {
        @Override
        public void matches(OccurredEvent event, boolean state, final FlagState result) {
            if(Dependencies.hasGriefPrevention()) {
                Player player = null;
                if (event.getTool() instanceof PlayerSubject) {
                    player = ((PlayerSubject) event.getTool()).getPlayer();
                }
                if (player != null) {
                    PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
                    Claim claim = null;
                    if(GriefPrevention.instance.dataStore.getClaimAt(event.getLocation(), true, playerData.lastClaim) != null)
                        claim = GriefPrevention.instance.dataStore.getClaimAt(event.getLocation(), true, playerData.lastClaim);
                    if(claim != null && claim.allowAccess(player) == null) {
                        Log.logInfo("GriefPrevention claim permission allowed.", HIGHEST);
                        result.dropThis = true;
                    } else if(claim != null && claim.allowAccess(player) != null) {
                        Log.logInfo("GriefPrevention claim permission failed.", HIGHEST);
                        result.dropThis = false;
                    }
                }
            }
        }
    };


    public final static class FlagState {
        public boolean dropThis         = true;
        public boolean continueDropping = true;
    };

    // LinkedHashMap because I want to preserve order
    private static Map<String, Flag> flags       = new LinkedHashMap<String, Flag>();
    private static int               nextOrdinal = 0;
    private int                      ordinal;
    private String                   name;
    private Plugin                   pl;

    static {
        flags.put("IN_MOB_ARENA", IN_MOB_ARENA);
        flags.put("UNIQUE", UNIQUE);
        flags.put("WORLDGUARD_BUILD_PERMISSION", WORLDGUARD_BUILD_PERMISSION);
        flags.put("TOWNY_BUILD_PERMISSION", TOWNY_BUILD_PERMISSION);
        flags.put("GRIEFPREVENTION_BUILD_PERMISSION", GRIEFPREVENTION_BUILD_PERMISSION);
    }

    private Flag(String tag) {
        name = tag;
        ordinal = nextOrdinal;
        nextOrdinal++;
        pl = OtherDrops.plugin;
    }

    protected Flag(Plugin plugin, String tag) {
        this(tag);
        if (plugin == null || plugin instanceof OtherDrops)
            throw new IllegalArgumentException("Use your own plugin for registering a flag!");
        pl = plugin;
    }

    /**
     * Register a new flag to your plugin.
     * 
     * @param flag
     *            The flag to register.
     */
    public static void register(Flag flag) {
        flags.put(flag.name, flag);
    }

    /**
     * Unregister a previously registered flag.
     * 
     * @param plugin
     *            The plugin that registered the action (preferably your
     *            plugin).
     * @param flag
     *            The flag to unregister.
     */
    public static void unregister(Plugin plugin, Flag flag) {
        if (!flag.pl.getClass().equals(plugin.getClass()))
            throw new IllegalArgumentException("You didn't register that flag!");
        flags.remove(flag.name);
    }

    public static Set<Flag> parseFrom(ConfigurationNode dropNode) {
        List<String> list = OtherDropsConfig.getMaybeList(dropNode, "flag", "flags");
        Set<Flag> set = new HashSet<Flag>();
        for (String flag : list) {
            Flag newFlag = flags.get(flag.toUpperCase());
            if (newFlag != null) {
                Log.logInfo("Adding valid flag: " + newFlag.toString(), Verbosity.HIGHEST);
                set.add(newFlag);
            } else {
                Log.logInfo("Invalid flag, ignoring (" + flag + ")", Verbosity.NORMAL);
            }
        }
        if(OtherDropsConfig.globalenablewgmatching)
            set.add(flags.get("WORLDGUARD_BUILD_PERMISSION"));
        return set;
    }

    @Override
    public final int compareTo(Flag other) {
        return Integer.valueOf(ordinal).compareTo(other.ordinal);
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof Flag))
            return false;
        return ordinal == ((Flag) other).ordinal;
    }

    @Override
    public final int hashCode() {
        return ordinal;
    }

    @Override
    public final String toString() {
        return name;
    }

    /**
     * Return a list of all valid flags.
     * 
     * @return All actions.
     */
    public static Flag[] values() {
        return flags.values().toArray(new Flag[0]);
    }

    /**
     * Return a list of all valid flag names.
     * 
     * @return All actions.
     */
    public static Set<String> getValidFlags() {
        return flags.keySet();
    }

    /**
     * Get a flag by name.
     * 
     * @param key
     *            The flag tag name.
     * @return The flag, or null if it does not exist.
     */
    public static Flag valueOf(String key) {
        return flags.get(key);
    }

    /**
     * Check if the flag applies to the given event. All registered flags will
     * be checked for each event, which means that this is called regardless of
     * whether the flag was set.
     * 
     * @param event
     *            A drop event to check against.
     * @param state
     *            Whether the flag is set on the event; typically you only do
     *            anything if this is true, but sometimes you also need to do
     *            something if it is false.
     * @param result
     *            The result of the check, including whether to drop this drop
     *            and whether to continue processing further drops. This
     *            parameter should be declared final.
     */
    public abstract void matches(OccurredEvent event, boolean state, final FlagState result);
}
