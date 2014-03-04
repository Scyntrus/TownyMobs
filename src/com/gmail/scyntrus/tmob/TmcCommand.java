package com.gmail.scyntrus.tmob;

import net.minecraft.server.v1_7_R1.Entity;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.gmail.scyntrus.tmob.mobs.Archer;
import com.gmail.scyntrus.tmob.mobs.Mage;
import com.gmail.scyntrus.tmob.mobs.Swordsman;
import com.gmail.scyntrus.tmob.mobs.Titan;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class TmcCommand implements CommandExecutor {

	TownyMobs plugin;
	
	public TmcCommand(TownyMobs plugin) {
		this.plugin = plugin;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		if (sender instanceof Player) {
			if (!sender.hasPermission("tmob.tmc")) {
				sender.sendMessage("You do not have permission");
				return true;
			}
		}
		
		if (TownyMobs.mobList.size() >= TownyMobs.spawnLimit) {
			sender.sendMessage("There are too many Towny mobs");
			return true;
		}
		
		if (split.length < 6) {
			sender.sendMessage("Not enough arguments");
			return false;
		}
		
		org.bukkit.World craftWorld = plugin.getServer().getWorld(split[2]);
		if (craftWorld==null) {
			sender.sendMessage("World not found");
			return false;
		}
		net.minecraft.server.v1_7_R1.World world = ((CraftWorld)craftWorld).getHandle();
		
		Location loc = null;
		try {
			loc = new Location(craftWorld, Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5]));
		} catch (Exception ex) {
			sender.sendMessage("Invalid coordinates");
			return false;
		}
		
		Town town = null;
		try {
			town = TownyUniverse.getDataSource().getTown(split[1]);
		} catch (Exception ex) {
			sender.sendMessage("Town not found");
			return false;
		}

		if (town == null) {
			sender.sendMessage("Town not found");
			return false;
		}
		
		TownyMob newMob = null;
		if (split[0].equalsIgnoreCase("Archer") || split[0].equalsIgnoreCase("Ranger")) {
			newMob = new Archer(loc, town);
		} else if (split[0].equalsIgnoreCase("Swordsman")) {
			newMob = new Swordsman(loc, town);
		} else if (split[0].equalsIgnoreCase("Titan") || split[0].equalsIgnoreCase("Golem")) {
			newMob = new Titan(loc, town);
		} else if (split[0].equalsIgnoreCase("Mage") || split[0].equalsIgnoreCase("Witch")) {
			newMob = new Mage(loc, town);
		} else {
			sender.sendMessage("Unrecognized mob name");
			return true;
		}
		
		if (!newMob.getEnabled()) {
			sender.sendMessage(String.format("Spawning %s has been disabled", newMob.getTypeName()));
			newMob.forceDie();
			return true;
		}
				
		world.addEntity((Entity) newMob, SpawnReason.CUSTOM);
		TownyMobs.mobList.add(newMob);
		
		if (split.length > 6) {
			if (split[6].equalsIgnoreCase("moveToPoint") || split[6].equalsIgnoreCase("move") || split[6].equalsIgnoreCase("point")) {
				if (split.length < 10) {
					sender.sendMessage("Not enough arguments for move order");
					return false;
				}
				try {
					newMob.setPoi(Double.parseDouble(split[7]), Double.parseDouble(split[8]), Double.parseDouble(split[9]));
					newMob.setOrder("poi");
				} catch (Exception ex) {
					sender.sendMessage("Invalid move coordinates");
					return false;
				}
				return true;
			} else if (split[6].equalsIgnoreCase("patrolHere") || split[6].equalsIgnoreCase("patrol")) {
				if (split.length < 10) {
					sender.sendMessage("Not enough arguments for patrol order");
					return false;
				}
				try {
					newMob.setPoi(Double.parseDouble(split[7]), Double.parseDouble(split[8]), Double.parseDouble(split[9]));
					newMob.setOrder("ppoi");
				} catch (Exception ex) {
					sender.sendMessage("Invalid patrol coordinates");
					return false;
				}
				return true;
			} else if (split[6].equalsIgnoreCase("path")) {
				if (split.length < 13) {
					sender.sendMessage("Not enough arguments for path order");
					return false;
				}
				try {
					newMob.setOrder("path");
					newMob.setPoi(Double.parseDouble(split[7]), Double.parseDouble(split[8]), Double.parseDouble(split[9]));
					Location spawnLoc = newMob.getSpawn();
					spawnLoc.setX(Double.parseDouble(split[10]));
					spawnLoc.setY(Double.parseDouble(split[11]));
					spawnLoc.setZ(Double.parseDouble(split[12]));
				} catch (Exception ex) {
					sender.sendMessage("Invalid path coordinates");
					return false;
				}
				return true;
			} else if (split[6].equalsIgnoreCase("wander")) {
				newMob.setOrder("wander");
				return true;
			}
		}
		return true;
	}
}
