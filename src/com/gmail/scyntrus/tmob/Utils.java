package com.gmail.scyntrus.tmob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityAnimal;
import net.minecraft.server.v1_7_R4.EntityCreeper;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.EntitySlime;
import net.minecraft.server.v1_7_R4.EntityWolf;
import net.minecraft.server.v1_7_R4.EntityZombie;
import net.minecraft.server.v1_7_R4.IMonster;
import net.minecraft.server.v1_7_R4.Item;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.NBTTagCompound;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class Utils {
	public static int TownCheck(Entity entity, Town town) {
		if (entity == null || town == null) {
			return 0;
		}
		if (entity.getBukkitEntity().hasMetadata("MyPet"))
			return 0;
		if (entity instanceof EntityPlayer) {
			Player player = ((EntityPlayer)entity).getBukkitEntity();
			if (player.getGameMode() == GameMode.CREATIVE) return 1;
			try {
				Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
				if (!resident.hasTown()) return 0;
				if (resident.getTown().equals(town)) return 1;
				if (!resident.hasNation()) return 0;
				if (resident.getTown().getNation().hasTown(town)) return 1;
				if (resident.getTown().getNation().hasAlly(town.getNation())) return 1;
				if (resident.getTown().getNation().hasEnemy(town.getNation())) return -1;
				if (TownyUniverse.isWarTime()) {
					if (resident.getTown().getNation().isNeutral()) return 0;
					if (town.getNation().isNeutral()) return 0;
					if (!resident.getTown().getNation().equals(town.getNation())) return -1;
				}
				return 0;
			} catch (Exception ex) {
				return 0;
			}
		} else if (entity instanceof TownyMob) {
			TownyMob tmob = (TownyMob) entity;
			try {
				if (tmob.getTown() == null) return 0;
				if (tmob.getTown().equals(town)) return 1;
				if (!tmob.getTown().hasNation()) return 0;
				if (tmob.getTown().getNation().hasTown(town)) return 1;
				if (tmob.getTown().getNation().hasAlly(town.getNation())) return 1;
				if (tmob.getTown().getNation().hasEnemy(town.getNation())) return -1;
				if (TownyUniverse.isWarTime()) {
					if (tmob.getTown().getNation().isNeutral()) return 0;
					if (town.getNation().isNeutral()) return 0;
					if (!tmob.getTown().getNation().equals(town.getNation())) return -1;
				}
				return 0;
			} catch (Exception ex) {
				return 0;
			}
		} else if (entity instanceof EntityWolf) {
			EntityWolf wolf = (EntityWolf) entity;
			if (wolf.isTamed()) {
				if (wolf.getOwner() != null) {
					return TownCheck(wolf.getOwner(), town);
				} else {
					return 0;
				}
			} else if (wolf.isAngry()) {
				return -1;
			} else {
				return 0;
			}
		} else if (entity instanceof EntityCreeper) {
			return 1;
		} else if (!TownyMobs.attackMobs) {
			return 0;
		} else if (entity instanceof EntityAnimal) {
			return 0;
		} else if (entity instanceof EntityZombie) {
			if (TownyMobs.attackZombies) {
				return -1;
			}
			return 0;
		} else if (entity instanceof EntitySlime) {
			EntitySlime slime = (EntitySlime) entity;
			if (slime.getSize() > 1) {
				return -1;
			} else {
				return 0;
			}
		} else if (entity instanceof IMonster) {
			return -1;
		}
		return 0;
	}
	
	public static void giveColorArmor(TownyMob entity) {
		int color = -1;
		if (entity.getTown() == null) {
			return;
		} else if (TownyMobs.townColors.containsKey(entity.getTown().getName())) {
			color = TownyMobs.townColors.get(entity.getTown().getName());
		} else {
			TownyMobs.townColors.put(entity.getTown().getName(), 10511680);
		}
		
		if (color == -1 || color == 10511680) {
			entity.getEntity().setEquipment(1, new ItemStack((Item)Item.REGISTRY.get("leather_boots")));
			entity.getEntity().setEquipment(2, new ItemStack((Item)Item.REGISTRY.get("leather_leggings")));
			entity.getEntity().setEquipment(3, new ItemStack((Item)Item.REGISTRY.get("leather_chestplate")));
			entity.getEntity().setEquipment(4, new ItemStack((Item)Item.REGISTRY.get("leather_helmet")));
			return;
		}
		
		ItemStack[] itemStacks = {
				new ItemStack((Item)Item.REGISTRY.get("leather_boots")), 
				new ItemStack((Item)Item.REGISTRY.get("leather_leggings")), 
				new ItemStack((Item)Item.REGISTRY.get("leather_chestplate")),
				new ItemStack((Item)Item.REGISTRY.get("leather_helmet"))};

	    for (ItemStack i : itemStacks) {
	    	NBTTagCompound n = i.getTag();
			if (n == null) {
				n = new NBTTagCompound();
				i.setTag(n);
			}
			NBTTagCompound n2 = n.getCompound("display");
			if (!n.hasKey("display"))
				n.set("display", n2);
			n2.setInt("color", color);
	    }
	    
        entity.setEquipment(1, itemStacks[0]);
        entity.setEquipment(2, itemStacks[1]);
        entity.setEquipment(3, itemStacks[2]);
        entity.setEquipment(4, itemStacks[3]);
        return;
	}
	
	public TownyMob mobCreate() { // not implemented yet
		return null;
	}
	
	public static double dist3D(double x1, double x2, double y1, double y2, double z1, double z2) {
		return Math.sqrt(Math.pow(x1-x2,2) + Math.pow(y1-y2,2) + Math.pow(z1-z2,2));
	}
	
	public static double countMobPowerInTown(Town town) {
		double power = 0;
		for (TownyMob tmob : TownyMobs.mobList) {
			if (tmob.getTownName().equals(town.getName())) {
				power += tmob.getPowerCost();
			}
		}
		return power;
	}
	
	public static int countMobsInTown(Town town) {
		int count = 0;
		for (TownyMob tmob : TownyMobs.mobList) {
			if (tmob.getTownName().equals(town.getName())) {
				count++;
			}
		}
		return count;
	}
	
	public static void copyDefaultConfig() {
		InputStream stream = Utils.class.getResourceAsStream("/config.yml");
		if (stream == null) {
			System.out.println("Unable to find default config.yml");
			return;
		}
		OutputStream resStreamOut = null;
		int readBytes;
		byte[] buffer = new byte[4096];
		try {
			resStreamOut = new FileOutputStream(new File(TownyMobs.instance.getDataFolder(), "configDefaults.yml"));
			while ((readBytes = stream.read(buffer)) > 0) {
				resStreamOut.write(buffer, 0, readBytes);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				resStreamOut.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
