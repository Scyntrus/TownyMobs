package com.gmail.scyntrus.tmob;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class CommandListener implements Listener {
	
	TownyMobs plugin;
	
	public CommandListener(TownyMobs plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		if (e.getMessage().toLowerCase().contains("f leave")
				|| e.getMessage().toLowerCase().contains("f kick")
				|| e.getMessage().toLowerCase().contains("f disband")
				|| e.getMessage().toLowerCase().contains("butcher")
				) {
	        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	        	public void run() {
	    			plugin.updateList();
	        	}
	        }, 0L);
		}
		if (e.getPlayer().isOp() && e.getMessage().toLowerCase().startsWith("save-all")) {
			plugin.saveMobList();
		}
		if (TownyMobs.excludeFromKillCommands && e.getMessage().toLowerCase().contains("kill")) {
			checkDeath();
		}
	}
	
	@EventHandler
	public void onServerCommand(ServerCommandEvent e) {
		if (e.getCommand().toLowerCase().startsWith("save-all")) {
			plugin.saveMobList();
		}
		if (TownyMobs.excludeFromKillCommands && e.getCommand().toLowerCase().contains("kill")) {
			checkDeath();
		}
	}
	
	public void checkDeath() {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DeadChecker(plugin), 1);
	}
}
