package com.gmail.scyntrus.tmob;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityInsentient;
import net.minecraft.server.v1_6_R3.EntityWolf;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.gmail.scyntrus.tmob.mobs.Titan;
import com.palmergames.bukkit.towny.event.MobRemovalEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class EntityListener implements Listener {
	
	TownyMobs plugin;
	
	public EntityListener(TownyMobs plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent e) {
		Entity entity = ((CraftEntity) e.getEntity()).getHandle();
		if (entity != null && entity instanceof TownyMob) {
			e.setCancelled(true);
			TownyMob fmob = (TownyMob) entity;
			if (fmob instanceof Titan) {
				fmob.findTarget();
				return;
			}
			if (e.getTarget() != null) {
				Entity target = ((CraftEntity) e.getTarget()).getHandle();
				if (Utils.TownCheck(target, fmob.getTown()) == -1) {
					fmob.setTarget(target);
					return;
				}
			}
			fmob.findTarget();
			return;
		} else if (entity != null && entity instanceof EntityWolf) {
			if (e.getTarget() != null) {
				Entity target = ((CraftEntity) e.getTarget()).getHandle();
				if (target instanceof TownyMob) {
					EntityWolf wolf = (EntityWolf) entity;
					TownyMob fmob = (TownyMob) target;
					if (wolf.isAngry()) {
						return;
					} else if (wolf.isTamed()) {
						if (wolf.getOwner() != null) {
							if (fmob.getGoalTarget().equals(wolf.getOwner())) {
								return;
							}
							switch (Utils.TownCheck(wolf.getOwner(), fmob.getTown())) {
							case 1:
							case 0:
								e.setCancelled(true);
								return;
							case -1:
								return;
							}
						} else {
							e.setCancelled(true);
							return;
						}
					}
					e.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent e) {
		if (((CraftEntity)e.getRightClicked()).getHandle() instanceof TownyMob) {
			TownyMob fmob = (TownyMob) ((CraftEntity)e.getRightClicked()).getHandle();
			if (fmob.getTown() == null) {
				return;
			}
			Player player = e.getPlayer();
			player.sendMessage(String.format("%sThis %s%s %sbelongs to town %s%s%s. HP: %s%s", 
					ChatColor.GREEN, ChatColor.RED, fmob.getTypeName(), ChatColor.GREEN, ChatColor.RED, 
					fmob.getTownName(), ChatColor.GREEN, ChatColor.RED, fmob.getHealth()));
			try {
				if (player.hasPermission("fmob.order") && TownyUniverse.getDataSource().getResident(player.getName()).getTown().equals(fmob.getTown())) {
					if (!plugin.playerSelections.containsKey(player.getName())) {
						plugin.playerSelections.put(player.getName(), new ArrayList<TownyMob>());
					}
					if (plugin.playerSelections.get(player.getName()).contains(fmob)) {
						plugin.playerSelections.get(player.getName()).remove(fmob);
						player.sendMessage(String.format("%sYou have deselected this %s%s", ChatColor.GREEN, ChatColor.RED, fmob.getTypeName()));
						if (plugin.playerSelections.get(player.getName()).isEmpty()) {
							plugin.playerSelections.remove(player.getName());
							player.sendMessage(String.format("%sYou have no mobs selected", ChatColor.GREEN));
						}
					} else {
						plugin.playerSelections.get(player.getName()).add(fmob);
						player.sendMessage(String.format("%sYou have selected this %s%s", ChatColor.GREEN, ChatColor.RED, fmob.getTypeName()));
						fmob.setPoi(fmob.getlocX(), fmob.getlocY(), fmob.getlocZ());
						fmob.setOrder("poi");
					}
				}
			} catch (Exception ex) {}
			fmob.updateMob();
			if (TownyMobs.feedEnabled) {
				if (player.getItemInHand().getType() == Material.APPLE) {
					player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
					float iHp = fmob.getHealth();
					fmob.setHealth(fmob.getHealth() + TownyMobs.feedAmount);
					player.sendMessage(String.format("%sThis mob has been healed by %s%s", ChatColor.GREEN, ChatColor.RED, fmob.getHealth() - iHp));
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		if (((CraftEntity) e.getEntity()).getHandle() instanceof TownyMob) {
			((CraftEntity) e.getEntity()).getHandle().die();
			e.getDrops().clear();
			e.getDrops().add(new ItemStack(((TownyMob) ((CraftEntity) e.getEntity()).getHandle()).getDrops()));
		}
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
        	public void run() {
    			plugin.updateList();
        	}
        }, 1L);
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof CraftLivingEntity)) return;
		CraftLivingEntity entity = (CraftLivingEntity) e.getEntity();
		CraftEntity damager = (CraftEntity) e.getDamager();
		if (damager instanceof Projectile) damager = (CraftEntity) ((Projectile) damager).getShooter();
		if (damager == null) return;
		
		if (damager.getHandle() instanceof TownyMob) {
			TownyMob fmob = (TownyMob) damager.getHandle();
			if (Utils.TownCheck(entity.getHandle(), fmob.getTown()) < 1) {
				if (fmob.isAlive()) {
					if (entity instanceof CraftCreature) {
						((CraftCreature) entity).getHandle().setTarget(((CraftLivingEntity) damager).getHandle());
					}
					if (entity.getHandle() instanceof EntityInsentient) {
						((EntityInsentient) entity.getHandle()).setGoalTarget(((CraftLivingEntity) damager).getHandle());
					}
					return;
				}
			} else if (TownyMobs.noFriendlyFire) {
				e.setCancelled(true);
				return;
			}
		} else if ((damager instanceof Player)
				&& (entity.getHandle() instanceof TownyMob)) {
			TownyMob fmob = (TownyMob) entity.getHandle();
			Player player = (Player) damager;
			try {
			if (Utils.TownCheck(((CraftPlayer) player).getHandle(), fmob.getTown()) >= 1) {
				if (fmob.getTown().equals(TownyUniverse.getDataSource().getResident(player.getName()).getTown())) {
					player.sendMessage(String.format("%sYou hit a friendly %s%s", ChatColor.YELLOW, ChatColor.RED, fmob.getTypeName()));
					fmob.getEntity().getBukkitEntity().setMetadata("NPC", new FixedMetadataValue(plugin, true));
					return;
				} else {
					player.sendMessage(String.format("%sYou cannot hit %s%s%s's %s%s", ChatColor.YELLOW, ChatColor.RED, fmob.getTownName(), ChatColor.YELLOW, ChatColor.RED, fmob.getTypeName()));
					e.setCancelled(true);
					return;
				}
			}
			} catch (Exception ex) {}
		}
		
		if (!TownyMobs.alertAllies || damager.isDead()) {
			return;
		}
		if (entity.getHandle() instanceof TownyMob) {
			Town town = ((TownyMob) entity.getHandle()).getTown();
			List<org.bukkit.entity.Entity> aoeList = entity.getNearbyEntities(8, 8, 8);
			for (org.bukkit.entity.Entity nearEntity : aoeList) {
				if (((CraftEntity) nearEntity).getHandle() instanceof TownyMob) {
					TownyMob fmob2 = (TownyMob) ((CraftEntity) nearEntity).getHandle();
					if (Utils.TownCheck(fmob2.getEntity(), town) == 1 && 
							Utils.TownCheck(damager.getHandle(), town) < 1) {
						fmob2.softAgro(damager.getHandle());
					}
				}
			}
		} else if (entity instanceof Player) {
			try {
				Town town = TownyUniverse.getDataSource().getResident(((Player) entity).getName()).getTown();
				List<org.bukkit.entity.Entity> aoeList = entity.getNearbyEntities(8, 8, 8);
				for (org.bukkit.entity.Entity nearEntity : aoeList) {
					if (((CraftEntity) nearEntity).getHandle() instanceof TownyMob) {
						TownyMob fmob2 = (TownyMob) ((CraftEntity) nearEntity).getHandle();
						if (Utils.TownCheck(fmob2.getEntity(), town) == 1 && 
								Utils.TownCheck(damager.getHandle(), town) < 1) {
							fmob2.softAgro(damager.getHandle());
						}
					}
				}
			} catch (Exception ex) {}
		}
	}
	
		@EventHandler(priority=EventPriority.MONITOR)
		public void onEntityDamageByEntity2(EntityDamageByEntityEvent e) {
			if (((CraftEntity) e.getEntity()).getHandle() instanceof TownyMob
					&& e.getEntity().hasMetadata("NPC")) {
				e.getEntity().removeMetadata("NPC", plugin);
			}
		}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		EntityDamageEvent lastDamage = e.getEntity().getLastDamageCause();
		if (lastDamage instanceof EntityDamageByEntityEvent) {
			org.bukkit.entity.Entity entity = ((EntityDamageByEntityEvent) lastDamage).getDamager();
			if (entity != null) {
				if (((CraftEntity) entity).getHandle() instanceof TownyMob) {
					TownyMob fmob = (TownyMob) ((CraftEntity) entity).getHandle();
					if (fmob.getTown() == null) {
						return;
					}
					e.setDeathMessage(e.getEntity().getDisplayName() + " was killed by " + ChatColor.RED + fmob.getTownName() + ChatColor.RESET + "'s " + ChatColor.RED + fmob.getTypeName());
				} else if (entity instanceof Projectile){
					Projectile arrow = (Projectile) entity;
					if (((CraftLivingEntity) arrow.getShooter()).getHandle() instanceof TownyMob) {
						TownyMob fmob = (TownyMob) ((CraftLivingEntity) arrow.getShooter()).getHandle();
						if (fmob.getTown() == null) {
							return;
						}
						e.setDeathMessage(e.getEntity().getDisplayName() + " was shot by " + ChatColor.RED + fmob.getTownName() + ChatColor.RESET + "'s " + ChatColor.RED + fmob.getTypeName());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if (plugin.playerSelections.containsKey(player.getName())) {
			plugin.playerSelections.get(player.getName()).clear();
			plugin.playerSelections.remove(player.getName());
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (plugin.mobLeader.containsKey(e.getPlayer().getName()) && plugin.playerSelections.containsKey(e.getPlayer().getName())) {
			if (e.getFrom().distance(e.getTo()) < 0.00001) {
				return;
			}
			Player player = e.getPlayer();
			Location loc = player.getLocation();
			int count = 0;
			for (TownyMob fmob : plugin.playerSelections.get(player.getName())) {
				if (fmob.getSpawn().getWorld().getName().equals(loc.getWorld().getName())) {
					double tmpX = (1.5-(count%4))*1.5;
					double tmpZ = ((-1.) - Math.floor(count / 4.))*1.5;
					double tmpH = Math.hypot(tmpX, tmpZ);
					double angle = Math.atan2(tmpZ, tmpX) + (loc.getYaw() * Math.PI / 180.);
					fmob.setPoi(loc.getX() + tmpH*Math.cos(angle), loc.getY(), loc.getZ() + tmpH*Math.sin(angle));
					count += 1;
				}
			}
		}
	}
	
	@EventHandler
	public void onPotionSplash(PotionSplashEvent e) {
		if (e.getPotion().getShooter() == null) return;
		if (((CraftEntity) e.getPotion().getShooter()).getHandle() instanceof TownyMob) {
			TownyMob fmob = (TownyMob) ((CraftEntity) e.getPotion().getShooter()).getHandle();
			for (LivingEntity entity : e.getAffectedEntities()) {
				if (Utils.TownCheck(((CraftEntity) entity).getHandle(), fmob.getTown()) < 1) {
					if (fmob.isAlive()) {
						if (entity instanceof CraftCreature) {
							((CraftCreature) entity).getHandle().setTarget(((CraftLivingEntity) e.getPotion().getShooter()).getHandle());
						}
						if (((CraftLivingEntity) entity).getHandle() instanceof EntityInsentient) {
							((EntityInsentient) ((CraftLivingEntity) entity).getHandle()).setGoalTarget(((CraftLivingEntity) e.getPotion().getShooter()).getHandle());
						}
					}
				} else if (TownyMobs.noFriendlyFire) {
					e.setIntensity(entity, -1);
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityPortal(EntityPortalEvent e) {
		if (((CraftEntity) e.getEntity()).getHandle() instanceof TownyMob) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onChunkLoad(ChunkLoadEvent e) {
		TownyMobs.scheduleChunkMobLoad = true;
		if (!plugin.getServer().getScheduler().isCurrentlyRunning(TownyMobs.chunkMobLoadTask) && 
				!plugin.getServer().getScheduler().isQueued(TownyMobs.chunkMobLoadTask)) {
			TownyMobs.chunkMobLoadTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new ChunkMobLoader(plugin), 1, 1);
		}
	}
	
	@EventHandler
	public void onTownyMobRemove(MobRemovalEvent e) {
		if (((CraftEntity) e.getEntity()).getHandle() instanceof TownyMob) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (TownyMobs.runKeepAliveTask && e.isCancelled() && ((CraftLivingEntity) e.getEntity()).getHandle() instanceof TownyMob) {
			e.setCancelled(false);
		}
	}
}
