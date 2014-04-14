package com.gmail.scyntrus.tmob;

import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.economy.EconomyResponse;
import net.minecraft.server.v1_7_R3.Entity;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.gmail.scyntrus.tmob.mobs.Archer;
import com.gmail.scyntrus.tmob.mobs.Mage;
import com.gmail.scyntrus.tmob.mobs.Swordsman;
import com.gmail.scyntrus.tmob.mobs.Titan;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class TmCommand implements CommandExecutor {

	TownyMobs plugin;
	
	public TmCommand(TownyMobs plugin) {
		this.plugin = plugin;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (split.length == 0) {
				return false;
			} else if (split[0].equalsIgnoreCase("help")) {
				//Basic help info here.
				player.sendMessage("/tm spawn [mob]");
				player.sendMessage("Mobs: Archer, Swordsman, Titan, Mage");
				player.sendMessage("/tm color [color]");
				player.sendMessage("Color is in RRGGBB format");
				player.sendMessage("/tm order [order]");
				player.sendMessage("Orders: gohome, follow, stop, patrolHere, wander, tpHome, tpHere");
				player.sendMessage("Before giving orders, you must select mobs by right-clicking them");
			} else if (split[0].equalsIgnoreCase("info")) {
				if (!player.hasPermission("tmob.spawn")) {
					player.sendMessage(ChatColor.RED + "You do not have permission to spawn towny mobs.");
				} else {
					player.sendMessage(ChatColor.GREEN + "You have permission to spawn towny mobs");
				}
				if (!player.hasPermission("tmob.order")) {
					player.sendMessage(ChatColor.RED + "You do not have permission to order town mobs.");
				} else {
					player.sendMessage(ChatColor.GREEN + "You have permission to order towny mobs");
				}
				player.sendMessage(ChatColor.BLUE + "Archer:");
				if (!Archer.enabled) {
					player.sendMessage(ChatColor.RED + "disabled");
				} else {
					if (plugin.vaultEnabled) player.sendMessage("cost: " + Archer.moneyCost);
					player.sendMessage("power: " + Archer.powerCost);
				}
				player.sendMessage(ChatColor.BLUE + "Swordsman:");
				if (!Swordsman.enabled) {
					player.sendMessage(ChatColor.RED + "disabled");
				} else {
					if (plugin.vaultEnabled) player.sendMessage("cost: " + Swordsman.moneyCost);
					player.sendMessage("power: " + Swordsman.powerCost);
				}
				player.sendMessage(ChatColor.BLUE + "Mage:");
				if (!Mage.enabled) {
					player.sendMessage(ChatColor.RED + "disabled");
				} else {
					if (plugin.vaultEnabled) player.sendMessage("cost: " + Mage.moneyCost);
					player.sendMessage("power: " + Mage.powerCost);
				}
				player.sendMessage(ChatColor.BLUE + "Titan:");
				if (!Titan.enabled) {
					player.sendMessage(ChatColor.RED + "disabled");
				} else {
					if (plugin.vaultEnabled) player.sendMessage("cost: " + Titan.moneyCost);
					player.sendMessage("power: " + Titan.powerCost);
				}
			} else if (split[0].equalsIgnoreCase("deselect")) {
				if (plugin.playerSelections.containsKey(player.getName())) {
					plugin.playerSelections.get(player.getName()).clear();
					plugin.playerSelections.remove(player.getName());
					player.sendMessage("Selection cleared");
					return true;
				}
				player.sendMessage("You have not selected any mob");
				return true;
			} else if (split[0].equalsIgnoreCase("selectall")) {
				if (!player.hasPermission("tmob.selectall")) {
					player.sendMessage(ChatColor.RED + "You do not have permission.");
				}
				if (plugin.playerSelections.containsKey(player.getName())) {
					plugin.playerSelections.get(player.getName()).clear();
				} else {
					plugin.playerSelections.put(player.getName(), new ArrayList<TownyMob>());
				}
				try {
					Resident fplayer = TownyUniverse.getDataSource().getResident(player.getName());
					for (TownyMob tmob : TownyMobs.mobList) {
						if (tmob.getTown().getName().equals(fplayer.getTown().getName())) {
							plugin.playerSelections.get(player.getName()).add(tmob);
						}
					}
				} catch (Exception ex) {
					player.sendMessage(String.format("%sYou are not a resident of a town", ChatColor.RED));
					return true;
				}
				player.sendMessage(String.format("%sYou have selected %s mobs", ChatColor.GREEN, plugin.playerSelections.get(player.getName()).size()));
				return true;
			} else if (split[0].equalsIgnoreCase("selection")) {
				if (plugin.playerSelections.containsKey(player.getName())) {
					player.sendMessage(ChatColor.GREEN + "== Selection: ==");
					for (TownyMob tmob : plugin.playerSelections.get(player.getName())) {
						if (tmob.isAlive()) player.sendMessage(ChatColor.RED + tmob.getTypeName());
					}
					player.sendMessage(ChatColor.GREEN + "================");
					return true;
				}
				player.sendMessage("You have not selected any mob");
				return true;
			} else if (split[0].equalsIgnoreCase("spawn")) {
				if (!player.hasPermission("tmob.spawn") &&
						!player.hasPermission("tmob.spawn.archer") &&
						!player.hasPermission("tmob.spawn.mage") &&
						!player.hasPermission("tmob.spawn.swordsman") &&
						!player.hasPermission("tmob.spawn.titan")) {
					player.sendMessage(ChatColor.RED + "You do not have permission.");
					return true;
				}
				Location loc = player.getLocation();
				Resident fplayer = null;
				Town playertown = null;
				try {
					fplayer = TownyUniverse.getDataSource().getResident(player.getName());
					playertown = fplayer.getTown();
				} catch (Exception ex) {
					player.sendMessage(ChatColor.RED + "You must be in a town.");
					return true;
				}
				if (playertown == null) {
					player.sendMessage(ChatColor.RED + "You must be in a town.");
					return true;
				}
				if (!player.hasPermission("tmob.bypass")) {
					String areatown = TownyUniverse.getTownName(loc);
					if (!playertown.getName().equals(areatown)) {
						player.sendMessage(ChatColor.RED + "You may only spawn mobs in your town");
						return true;
					}
					if (TownyMobs.mobList.size() >= TownyMobs.spawnLimit) {
						player.sendMessage(ChatColor.RED + "There are too many towny mobs");
						return true;
					}
					if (TownyMobs.mobsPerTown > 0) {
						if (Utils.countMobsInTown(playertown) >= TownyMobs.mobsPerTown) {
							player.sendMessage(ChatColor.RED + "Your town has too many towny mobs.");
							return true;
						}
					}
				}
				net.minecraft.server.v1_7_R3.World world = ((CraftWorld)player.getWorld()).getHandle();
				TownyMob newMob = null;
				if (split.length == 1) {
					player.sendMessage(ChatColor.RED + "You must specify a mob");
					return true;
				} else if (split[1].equalsIgnoreCase("Archer") || split[1].equalsIgnoreCase("Ranger")) {
					if (!player.hasPermission("tmob.spawn") && !player.hasPermission("tmob.spawn.archer")) {
						player.sendMessage(ChatColor.RED + "You do not have permission to spawn this mob.");
						return false;
					}
					newMob = new Archer(player.getLocation(), playertown);
				} else if (split[1].equalsIgnoreCase("Swordsman")) {
					if (!player.hasPermission("tmob.spawn") && !player.hasPermission("tmob.spawn.swordsman")) {
						player.sendMessage(ChatColor.RED + "You do not have permission to spawn this mob.");
						return false;
					}
					newMob = new Swordsman(player.getLocation(), playertown);
				} else if (split[1].equalsIgnoreCase("Titan") || split[1].equalsIgnoreCase("Golem")) {
					if (!player.hasPermission("tmob.spawn") && !player.hasPermission("tmob.spawn.titan")) {
						player.sendMessage(ChatColor.RED + "You do not have permission to spawn this mob.");
						return false;
					}
					newMob = new Titan(player.getLocation(), playertown);
				} else if (split[1].equalsIgnoreCase("Mage") || split[1].equalsIgnoreCase("Witch")) {
					if (!player.hasPermission("tmob.spawn") && !player.hasPermission("tmob.spawn.mage")) {
						player.sendMessage(ChatColor.RED + "You do not have permission to spawn this mob.");
						return false;
					}
					newMob = new Mage(player.getLocation(), playertown);
				} else {
					player.sendMessage(ChatColor.RED + "Unrecognized mob name");
					return true;
				}
				if (!newMob.getEnabled()) {
					player.sendMessage(String.format("%sSpawning %s has been disabled", ChatColor.RED, newMob.getTypeName()));
					newMob.forceDie();
					return true;
				}
				
				if (!player.hasPermission("tmob.bypass")) {
					if (newMob.getPowerCost() > 0) {
						double townPowerUsage = Utils.countMobPowerInTown(playertown);
						if (playertown.getTotalBlocks() >= (townPowerUsage + newMob.getPowerCost())) {
			            	player.sendMessage(String.format("%sYour town is now using %s/%s power for towny mobs.", 
			            			ChatColor.GREEN, Math.round(townPowerUsage + newMob.getPowerCost()), playertown.getTotalBlocks()));
						} else {
			            	player.sendMessage(String.format("%sYour town is using %s/%s power for towny mobs.", 
			            			ChatColor.RED, Math.round(townPowerUsage), playertown.getTotalBlocks()));
			            	player.sendMessage(String.format("%sYou need %s more power.", ChatColor.RED, Math.round(townPowerUsage + newMob.getPowerCost() - playertown.getTotalBlocks())));
			                return true;
						}
					}
					if (plugin.vaultEnabled && newMob.getMoneyCost() > 0) {
						if (plugin.econ.has(player.getName(), newMob.getMoneyCost())) {
				            EconomyResponse r = plugin.econ.withdrawPlayer(player.getName(), newMob.getMoneyCost());
				            if(r.transactionSuccess()) {
				            	player.sendMessage(String.format("You paid %s and now have %s", plugin.econ.format(r.amount), plugin.econ.format(r.balance)));
				            } else {
				            	player.sendMessage(String.format("An error occured: %s", r.errorMessage));
				            	plugin.getLogger().severe(String.format("Unable to deduct money from %s", player.getName()));
				                return true;
				            }
						} else {
			            	player.sendMessage(ChatColor.RED + "You don't have enough money");
			                return true;
						}
					}
				}
				
				if (world.addEntity((Entity) newMob, SpawnReason.CUSTOM)) {
					TownyMobs.mobList.add(newMob);
					player.sendMessage(String.format("You have spawned a %s", newMob.getTypeName()));
				} else {
					newMob.forceDie();
					player.sendMessage(String.format("%sYou have failed to spawn a %s", ChatColor.RED, newMob.getTypeName()));
					if (!player.hasPermission("tmob.bypass")) {
						if (plugin.vaultEnabled && newMob.getMoneyCost() > 0) {
				            EconomyResponse r = plugin.econ.depositPlayer(player.getName(), newMob.getMoneyCost());
				            if(r.transactionSuccess()) {
				            	player.sendMessage(String.format("You have been refunded %s and now have %s", plugin.econ.format(r.amount), plugin.econ.format(r.balance)));
				            } else {
				            	player.sendMessage(String.format("An error occured: %s", r.errorMessage));
				            	plugin.getLogger().severe(String.format("Unable to refund money to %s", player.getName()));
				                return true;
				            }
						}
					}
				}
			} else if (split[0].equalsIgnoreCase("color")) {
				if (!player.hasPermission("tmob.color")) {
					player.sendMessage(ChatColor.RED + "You do not have permission");
					return true;
				}
				Resident fplayer = null;
				Town playertown = null;
				try {
					fplayer = TownyUniverse.getDataSource().getResident(player.getName());
					playertown = fplayer.getTown();
				} catch (Exception ex) {
					player.sendMessage(ChatColor.RED + "You must be in a town");
					return true;
				}
				if (playertown == null) {
					player.sendMessage(ChatColor.RED + "You must be in a town");
					return true;
				}
				if (split.length == 1) {
					player.sendMessage(ChatColor.RED + "You must specify a color in RRGGBB format");
					return true;
				} else {
					try {
						int myColor = Integer.parseInt(split[1], 16);
						TownyMobs.townColors.put(playertown.getName(), myColor);
						player.sendMessage(String.format("Set your town color to %s", StringUtils.leftPad(Integer.toHexString(myColor), 6, "0")));
						plugin.updateList();
					} catch (NumberFormatException e) {
						player.sendMessage(ChatColor.RED + "Invalid number");
						return true;
					}
				}
			} else if (split[0].equalsIgnoreCase("u")) {
				if (player.isOp()) {
					plugin.updateList();
					player.sendMessage(ChatColor.GREEN + "Towny Mobs refreshed");
				}
			} else if (split[0].equalsIgnoreCase("s")) {
				if (player.isOp()) {
					plugin.saveMobList();
					player.sendMessage(ChatColor.GREEN + "Towny Mobs data saved");
					System.out.println("Towny Mobs data saved via command");
				}
			} else if (split[0].equalsIgnoreCase("order")) {
				if (!player.hasPermission("tmob.order")) {
					player.sendMessage(ChatColor.RED + "You do not have permission");
					return true;
				}
				if (split.length < 2) {
					player.sendMessage(ChatColor.RED + "You must specify an order");
					player.sendMessage("Orders: gohome, follow, stop, patrolHere, wander, setHome, tpHome, tpHere");
					return true;
				} else if (!plugin.playerSelections.containsKey(player.getName())) {
					player.sendMessage(ChatColor.RED + "No mobs selected");
					player.sendMessage(ChatColor.RED + "Before giving orders, you must select mobs by right-clicking them");
					return true;
				} else {
					Resident fplayer = null;
					Town playertown = null;
					try {
						fplayer = TownyUniverse.getDataSource().getResident(player.getName());
						playertown = fplayer.getTown();
					} catch (Exception ex) {
						player.sendMessage(ChatColor.RED + "You must be in a town.");
						return true;
					}
					List<TownyMob> selection = plugin.playerSelections.get(player.getName());
					for (int i = selection.size()-1; i >= 0; i--) {
						if (!selection.get(i).isAlive()
								|| !selection.get(i).getTownName().equals(playertown.getName())) {
							selection.remove(i);
						}
					}
					if (selection.isEmpty()) {
						plugin.playerSelections.remove(player.getName());
						player.sendMessage(ChatColor.RED + "No mobs selected");
						return true;
					}
				}
				
				if (split[1].equalsIgnoreCase("gohome") || split[1].equalsIgnoreCase("home")) {
					plugin.mobLeader.remove(player.getName());
					for (TownyMob tmob : plugin.playerSelections.get(player.getName())) {
						tmob.setOrder("home");
						Location loc = tmob.getSpawn();
						tmob.setPoi(loc.getX(), loc.getY(), loc.getZ());
					}
					player.sendMessage(ChatColor.GREEN + "You sent your mobs home");
					return true;
				} else if (split[1].equalsIgnoreCase("follow")) {
					plugin.mobLeader.put(player.getName(), true);
					Location loc = player.getLocation();
					int count = 0;
					for (TownyMob tmob : plugin.playerSelections.get(player.getName())) {
						if (tmob.getSpawn().getWorld().getName().equals(loc.getWorld().getName())) {
							double tmpX = (1.5-(count%4))*1.5;
							double tmpZ = ((-1.) - Math.floor(count / 4.))*1.5;
							double tmpH = Math.hypot(tmpX, tmpZ);
							double angle = Math.atan2(tmpZ, tmpX) + (loc.getYaw() * Math.PI / 180.);
							tmob.setPoi(loc.getX() + tmpH*Math.cos(angle), loc.getY(), loc.getZ() + tmpH*Math.sin(angle));
							tmob.setOrder("poi");
							count += 1;
						}
					}
					player.sendMessage(ChatColor.GREEN + "Your mobs are now following you");
					return true;
				} else if (split[1].equalsIgnoreCase("stop")) {
					plugin.mobLeader.remove(player.getName());
					for (TownyMob tmob : plugin.playerSelections.get(player.getName())) {
						tmob.setOrder("poi");
					}
					player.sendMessage(ChatColor.GREEN + "You told your mobs to stop");
					return true;
				} else if (split[1].equalsIgnoreCase("moveToPoint") || split[1].equalsIgnoreCase("move") || split[1].equalsIgnoreCase("point")) {
					if (!player.hasPermission("tmob.order.move")) {
						player.sendMessage(ChatColor.RED + "You do not have permission");
						return true;
					}
					plugin.mobLeader.remove(player.getName());
					@SuppressWarnings("deprecation")
					Block block = player.getTargetBlock(null, 64);
					if (block == null) {
						player.sendMessage(ChatColor.RED + "You must be pointing at a block");
						return true;
					}
					Location loc = block.getLocation().add(0,1,0);
					Location playerLoc = player.getLocation();
					int count = 0;
					for (TownyMob tmob : plugin.playerSelections.get(player.getName())) {
						if (tmob.getSpawn().getWorld().getName().equals(playerLoc.getWorld().getName())) {
							double tmpX = (1.5-(count%4))*1.5;
							double tmpZ = ((-1.) - Math.floor(count / 4.))*1.5;
							double tmpH = Math.hypot(tmpX, tmpZ);
							double angle = Math.atan2(tmpZ, tmpX) + (playerLoc.getYaw() * Math.PI / 180.);
							tmob.setPoi(loc.getX() + tmpH*Math.cos(angle), loc.getY(), loc.getZ() + tmpH*Math.sin(angle));
							tmob.setOrder("poi");
							count += 1;
						}
					}
					player.sendMessage(ChatColor.GREEN + "You told your mobs to move where you're pointing");
					return true;
				} else if (split[1].equalsIgnoreCase("patrolHere") || split[1].equalsIgnoreCase("patrol")) {
					plugin.mobLeader.remove(player.getName());
					Location loc = player.getLocation();
					for (TownyMob tmob : plugin.playerSelections.get(player.getName())) {
						if (tmob.getSpawn().getWorld().getName().equals(loc.getWorld().getName())) {
							tmob.setOrder("ppoi");
							tmob.setPoi(loc.getX(), loc.getY(), loc.getZ());
						} else {
							player.sendMessage(String.format("%s%s is on a different world", ChatColor.RED, tmob.getTypeName()));
						}
					}
					player.sendMessage(ChatColor.GREEN + "Your mobs will now patrol from their home to here");
					return true;
				} else if (split[1].equalsIgnoreCase("wander")) {
					plugin.mobLeader.remove(player.getName());
					for (TownyMob tmob : plugin.playerSelections.get(player.getName())) {
						tmob.setOrder("wander");
					}
					player.sendMessage(ChatColor.GREEN + "Your mobs will now wander around");
					return true;
				} else if (split[1].equalsIgnoreCase("setHome")) {
					plugin.mobLeader.put(player.getName(), true);
					Location loc = player.getLocation();
					for (TownyMob tmob : plugin.playerSelections.get(player.getName())) {
						if (tmob.getSpawn().getWorld().equals(loc.getWorld())) {
							tmob.setOrder("home");
							Location spawnLoc = tmob.getSpawn();
							spawnLoc.setX(loc.getX());
							spawnLoc.setY(loc.getY());
							spawnLoc.setZ(loc.getZ());
							tmob.setPoi(spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ());
						} else {
							player.sendMessage(String.format("%s%s is on a different world", ChatColor.RED, tmob.getTypeName()));
						}
					}
					player.sendMessage(ChatColor.GREEN + "You set your position as your mob's new home");
					return true;
				} else if (split[1].equalsIgnoreCase("tpHome")) {
					plugin.mobLeader.remove(player.getName());
					if (!player.hasPermission("tmob.order.tp")) {
						player.sendMessage(ChatColor.RED + "You do not have permission");
						return true;
					}
					for (TownyMob tmob : plugin.playerSelections.get(player.getName())) {
						tmob.setOrder("home");
						Location loc = tmob.getSpawn();
						tmob.setPosition(loc.getX(), loc.getY(), loc.getZ());
						tmob.setPoi(loc.getX(), loc.getY(), loc.getZ());
					}
					player.sendMessage(ChatColor.GREEN + "Your mobs are now back at their home");
					return true;
				} else if (split[1].equalsIgnoreCase("tpHere")) {
					plugin.mobLeader.put(player.getName(), true);
					if (!player.hasPermission("tmob.order.tp")) {
						player.sendMessage(ChatColor.RED + "You do not have permission");
						return true;
					}
					Location loc = player.getLocation();
					int count = 0;
					for (TownyMob tmob : plugin.playerSelections.get(player.getName())) {
						if (tmob.getSpawn().getWorld().equals(loc.getWorld())) {
							double tmpX = (1.5-(count%4))*1.5;
							double tmpZ = ((-1.) - Math.floor(count / 4.))*1.5;
							double tmpH = Math.hypot(tmpX, tmpZ);
							double angle = Math.atan2(tmpZ, tmpX) + (loc.getYaw() * Math.PI / 180.);
							tmpX = loc.getX() + tmpH*Math.cos(angle);
							tmpZ = loc.getZ() + tmpH*Math.sin(angle);
							tmob.setPoi(tmpX, loc.getY(), tmpZ);
							tmob.setPosition(tmpX, loc.getY(), tmpZ);
							tmob.setOrder("poi");
							count++;
						} else {
							player.sendMessage(String.format("%s%s is on a different world", ChatColor.RED, tmob.getTypeName()));
						}
					}
					player.sendMessage("Your mobs are now with you");
					return true;
				} else if (split[1].equalsIgnoreCase("forgive")) {
					for (TownyMob tmob : plugin.playerSelections.get(player.getName())) {
						tmob.clearAttackedBy();
					}
				} else {
					player.sendMessage(ChatColor.RED + "Unrecognized order");
					player.sendMessage("Orders: gohome, follow, stop, patrolHere, wander, setHome, tpHome, tpHere");
					return true;
				}
			} else {
				player.sendMessage(ChatColor.RED + "Unrecognized command");
				return false;
			}
		} else {
			sender.sendMessage("You must be a player");
			return false;
		}
		return true;
	}
}
