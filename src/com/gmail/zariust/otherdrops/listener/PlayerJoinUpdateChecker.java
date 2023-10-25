package com.gmail.zariust.otherdrops.listener;

import org.bukkit.Bukkit;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.OtherDropsConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinUpdateChecker implements Listener {
	private final OtherDrops plugin;

	public PlayerJoinUpdateChecker(OtherDrops plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinUpdateCheck(PlayerJoinEvent evt) {
    	Player player = evt.getPlayer(); 
    	if(player.hasPermission("otherdrops.admin.updates") && OtherDropsConfig.globalUpdateChecking) {
			Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> plugin.updateChecker.checkForUpdate(evt.getPlayer()), 15L);
    	}
    }

}
