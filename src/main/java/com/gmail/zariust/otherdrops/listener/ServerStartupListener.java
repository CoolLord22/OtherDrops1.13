package main.java.com.gmail.zariust.otherdrops.listener;

import main.java.com.gmail.zariust.otherdrops.Log;
import main.java.com.gmail.zariust.otherdrops.OtherDrops;
import main.java.com.gmail.zariust.otherdrops.OtherDropsConfig;
import main.java.com.gmail.zariust.otherdrops.Updater;
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
        if (OtherDropsConfig.exportEnumLists) plugin.exportEnumLists();
        if (OtherDropsConfig.globalUpdateChecking) {
            plugin.updateChecker = new Updater(plugin);
            plugin.updateChecker.checkForUpdate(null);
        }
    }
}
