// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Zarius Tularial
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

package com.gmail.zariust.otherdrops;

import com.bgsoftware.wildstacker.api.WildStacker;
import com.gamingmesh.jobs.Jobs;
import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.MobArenaHandler;
import com.gmail.nossr50.mcMMO;
import com.gmail.zariust.common.Verbosity;
import com.herocraftonline.heroes.Heroes;
import com.palmergames.bukkit.towny.Towny;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import fr.neatmonster.nocheatplus.NoCheatPlus;
import me.drakespirit.plugins.moneydrop.MoneyDrop;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import static com.gmail.zariust.common.Verbosity.*;

@SuppressWarnings("unused")
public class Dependencies {
	// Plugin Dependencies
	private static WorldGuardPlugin worldGuard      = null; // for WorldGuard
	// support
	private static Towny	 		towny 			= null;
	private static WildStacker	    wildStacker 	= null;
	private static Jobs	 			jobs 			= null;
	private static NoCheatPlus		ncp 			= null;
	private static GriefPrevention  gp 				= null;

	boolean                         enabled;
	private static MobArena         mobArena        = null;
	private static MobArenaHandler  mobArenaHandler = null; // for MobArena
	private static MoneyDrop        moneyDrop       = null; // for MoneyDrop


	private static Economy          vaultEcon       = null;
	private static Permission       vaultPerms      = null;

	static String                   foundPlugins;
	static String                   notFoundPlugins;
	private static Heroes           heroes;

	private static think.rpgitems.Plugin          rpgItems        = null;
	private static mcMMO            mcmmo           = null;

	public static void init() {
		try {
			foundPlugins = "";
			notFoundPlugins = ""; // need to reset variables to allow for
			// reloads
			worldGuard = (WorldGuardPlugin) getPlugin("WorldGuard");
		} catch (Exception e) {
			Log.logInfo("Failed to load one or more optional dependencies - continuing OtherDrops startup.");
			e.printStackTrace();
		}
		try {
			towny = (Towny) getPlugin("Towny");
			wildStacker = (WildStacker) getPlugin("WildStacker");
			gp = (GriefPrevention) getPlugin("GriefPrevention");
			jobs = (Jobs) getPlugin("Jobs");
			ncp = (NoCheatPlus) getPlugin("NoCheatPlus");
			mobArena = (MobArena) getPlugin("MobArena");
			moneyDrop = (MoneyDrop) getPlugin("MoneyDrop");
			heroes = (Heroes) getPlugin("Heroes");
			rpgItems = (think.rpgitems.Plugin) getPlugin("RPG Items");
			mcmmo = (mcMMO) getPlugin("mcMMO");
		} catch (Exception e) {
			Log.logInfo("Failed to load one or more optional dependencies - continuing OtherDrops startup.");
			e.printStackTrace();
		}

		try {
			setupVault();
		} catch (Exception e) {
			Log.logInfo("Failed to load one or more optional dependencies - continuing OtherDrops startup.");
			e.printStackTrace();
		}

		try {
			if (mobArena != null) {
				mobArenaHandler = new MobArenaHandler();
			}

		} catch (Exception e) {
			Log.logInfo("Failed to load one or more optional dependencies - continuing OtherDrops startup.");
			e.printStackTrace();
		}
		if (!foundPlugins.isEmpty())
			Log.logInfo("Found supported plugin(s): '" + foundPlugins + "'",
					Verbosity.NORMAL);
		if (!notFoundPlugins.isEmpty())
			Log.logInfo("(Optional) plugin(s) not found: '" + notFoundPlugins
					+ "' (OtherDrops will continue to load)",
					Verbosity.HIGHEST);
	}

	public static Plugin getPlugin(String name) {
		Plugin plugin = OtherDrops.plugin.getServer().getPluginManager().getPlugin(name);

		if (plugin == null) {
			if (notFoundPlugins.isEmpty())
				notFoundPlugins += name;
			else
				notFoundPlugins += ", " + name;
		} else {
			if (foundPlugins.isEmpty())
				foundPlugins += name;
			else
				foundPlugins += ", " + name;
		}

		return plugin;
	}

	public static boolean hasPermission(Permissible who, String permission) {
		if (who instanceof ConsoleCommandSender)
			return true;
		boolean perm = who.hasPermission(permission);
		if (!perm) {
			Log.logInfo("SuperPerms - permission (" + permission
					+ ") denied for " + who.toString(), HIGHEST);
		} else {
			Log.logInfo("SuperPerms - permission (" + permission
					+ ") allowed for " + who.toString(), HIGHEST);
		}
		return perm;
	}

	private static void setupVault() {
		if (OtherDrops.plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
			vaultEcon = null;
			Log.logInfo("Couldn't load Vault.", EXTREME); // Vault's not
			// essential so no
			// need to worry.
			return;
		}
		Log.logInfo("Hooked into Vault.", HIGH);
		RegisteredServiceProvider<Economy> rsp = OtherDrops.plugin.getServer()
				.getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			vaultEcon = null;
			Log.logWarning("Found Vault but couldn't hook into Vault economy module (note: you need a separate economy plugin, eg. Essentials, iConomy, BosEconomy, etc.)",
					Verbosity.NORMAL);
			return;
		}
		vaultEcon = rsp.getProvider();

		// RegistereredServiceProvider<Chat> rsp =
		// getServer().getServicesManager().getRegistration(Chat.class);
		// chat = rsp.getProvider();
		// return chat != null;

		RegisteredServiceProvider<Permission> rsp_perms = OtherDrops.plugin
				.getServer().getServicesManager()
				.getRegistration(Permission.class);
		if (rsp_perms == null) {
			vaultPerms = null;
			Log.logWarning("...couldn't hook into Vault permissions module.",
					Verbosity.NORMAL);
			return;
		}
		vaultPerms = rsp_perms.getProvider();
	}

	public static boolean hasWildStacker() {
		return Dependencies.wildStacker != null;
	}

	public static boolean hasTowny() {
		return Dependencies.towny != null;
	}

	public static Towny getTowny() {
		return Dependencies.towny;
	}

	public static boolean hasJobs() {
		return Dependencies.jobs != null;
	}

	public static Jobs getJobs() {
		return Dependencies.jobs;
	}

	public static boolean hasGriefPrevention() {
		return Dependencies.gp != null;
	}

	public static GriefPrevention getGriefPrevention() {
		return Dependencies.gp;
	}

	public static boolean hasNCP() {
		return Dependencies.ncp != null;
	}

	public static NoCheatPlus getNCP() {
		return Dependencies.ncp;
	}

	public static boolean hasMobArena() {
		return Dependencies.mobArena != null;
	}

	public static MobArenaHandler getMobArenaHandler() {
		return Dependencies.mobArenaHandler;
	}

	public static boolean hasWorldGuard() {
		return Dependencies.worldGuard != null;
	}

	public static WorldGuardPlugin getWorldGuard() {
		return Dependencies.worldGuard;
	}

	public static boolean hasVaultEcon() {
		return Dependencies.vaultEcon != null;
	}

	public static Economy getVaultEcon() {
		return Dependencies.vaultEcon;
	}

	public static boolean hasMoneyDrop() {
		return Dependencies.moneyDrop != null;
	}

	public static MoneyDrop getMoneyDrop() {
		return Dependencies.moneyDrop;
	}

	public static boolean hasHeroes() {
		return Dependencies.heroes != null;
	}

	public static Heroes getHeroes() {
		return Dependencies.heroes;
	}

	public static think.rpgitems.Plugin getRpgItems() {
		return rpgItems;
	}

	public static boolean hasRpgItems() {
		return rpgItems != null;
	}

	public static mcMMO getMcmmo() {
		return mcmmo;
	}

	public static boolean hasMcmmo() {
		return mcmmo != null;
	}
}
