package com.gmail.zariust.otherdrops.listener;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerStartupListener implements Listener {
	private final OtherDrops plugin;

	public ServerStartupListener(OtherDrops plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onServerStart(ServerLoadEvent event) {
		Log.logInfo("Server startup finished, parsing config values.");
		plugin.registerParameters();
		plugin.initConfig();
		plugin.registerCommands();
	}
}
