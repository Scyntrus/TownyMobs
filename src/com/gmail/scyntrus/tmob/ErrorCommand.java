package com.gmail.scyntrus.tmob;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ErrorCommand implements CommandExecutor {
	
	TownyMobs plugin;
	
	public ErrorCommand(TownyMobs plugin) {
		this.plugin = plugin;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		sender.sendMessage(ChatColor.RED + "Towny Mobs is not compatible with this version of CraftBukkit. Please inform your server admin.");
		return true;
	}
}
