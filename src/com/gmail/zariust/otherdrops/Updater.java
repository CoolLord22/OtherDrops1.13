package com.gmail.zariust.otherdrops;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.gmail.zariust.common.Verbosity;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Updater {

	private final OtherDrops javaPlugin;
	private String localPluginVersion;
	private String spigotPluginVersion;

	//Constants. Customize to your liking.
	private static final int ID = 51793; //The ID of your resource. Can be found in the resource URL.
	private static final String ERR_MSG = "&cUpdate checker failed!";

	public Updater(OtherDrops javaPlugin) {
		this.javaPlugin = javaPlugin;
		this.localPluginVersion = javaPlugin.getDescription().getVersion();
	}

	public void checkForUpdate(Player p) {
		List<String> UPDATE_MSG = new ArrayList<String>();
		//The request is executed asynchronously as to not block the main thread.
		Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
			UPDATE_MSG.clear();
			//Request the current version of your plugin on SpigotMC.
			try {
				final HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + ID).openConnection();
				connection.setRequestMethod("GET");
				spigotPluginVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
			} catch (final IOException e) {
				UPDATE_MSG.add(ERR_MSG);
				if(OtherDropsConfig.getVerbosity().exceeds(Verbosity.HIGHEST))
					e.printStackTrace();
			}

			try {
				//Check if the requested version is the same as the one in your plugin.yml.
				if (localPluginVersion.equals(spigotPluginVersion)) {
					UPDATE_MSG.add(ChatColor.GREEN + "Hooray! You are running the latest version!");
				} else if(spigotPluginVersion.contains("b")) {
					Double spigot = Double.parseDouble(spigotPluginVersion.substring(0, spigotPluginVersion.indexOf("-b")));
					Double local = Double.parseDouble(localPluginVersion.substring(0, localPluginVersion.indexOf("-b")));
					if(spigot > local) {
						UPDATE_MSG.add(ChatColor.YELLOW + "A new release is available! " + ChatColor.RED + "Latest Version: " + ChatColor.GREEN + spigotPluginVersion + ChatColor.RED + " Your Version: " + ChatColor.GREEN + localPluginVersion);
						UPDATE_MSG.add(ChatColor.YELLOW + "Please download latest version from: " + ChatColor.GREEN + "https://www.spigotmc.org/resources/63497/updates");
					} else if(spigot.equals(local) && Integer.parseInt(spigotPluginVersion.substring(spigotPluginVersion.indexOf("b") + 1)) > Integer.parseInt(localPluginVersion.substring(localPluginVersion.indexOf("b") + 1))) {
						UPDATE_MSG.add(ChatColor.YELLOW + "A new build is available! " + ChatColor.RED + "Latest Version: " + ChatColor.GREEN + spigotPluginVersion + ChatColor.RED + " Your Version: " + ChatColor.GREEN + localPluginVersion);
						UPDATE_MSG.add(ChatColor.YELLOW + "Please download latest version from: " + ChatColor.GREEN + "https://www.spigotmc.org/resources/63497/updates");
					} else {
						UPDATE_MSG.add(ChatColor.RED + "Latest Version: " + ChatColor.GREEN + spigotPluginVersion + ChatColor.RED + " Your Version: " + ChatColor.GREEN + localPluginVersion);
						UPDATE_MSG.add(ChatColor.RED + "Beta builds aren't always stable. Use at your own risk!");
					}
				} else if(Integer.parseInt(spigotPluginVersion.replaceAll("\\.","")) > Integer.parseInt(localPluginVersion.substring(0, localPluginVersion.indexOf("-b")).replaceAll("\\.",""))) {
					UPDATE_MSG.add(ChatColor.RED + "Latest Version: " + ChatColor.GREEN + spigotPluginVersion + ChatColor.RED + " Your Version: " + ChatColor.GREEN + localPluginVersion);
					UPDATE_MSG.add(ChatColor.YELLOW + "Please download latest version from: " + ChatColor.GREEN + "https://www.spigotmc.org/resources/otherdrops-best-free-drop-manager.51793/updates");
				} else {
					UPDATE_MSG.add(ChatColor.RED + "Latest Version: " + ChatColor.GREEN + spigotPluginVersion + ChatColor.RED + " Your Version: " + ChatColor.GREEN + localPluginVersion);
					UPDATE_MSG.add(ChatColor.RED + "Beta builds aren't always stable. Use at your own risk!");
				}
			} catch (final NumberFormatException e) {
				UPDATE_MSG.add(ERR_MSG);
				if(OtherDropsConfig.getVerbosity().exceeds(Verbosity.HIGHEST))
					e.printStackTrace();
			}
			if(!UPDATE_MSG.isEmpty()) {
				for(String line : UPDATE_MSG) {
					if(p == null)
						Log.logInfo(line, Verbosity.LOW);
					if(p != null)
						if(!line.contains("Hooray"))
							p.sendMessage(ChatColor.GREEN + "[OtherDrops] " + line);
				}
			}
		});
	}
}
