package com.gmail.scyntrus.fmob;

import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityAnimal;
import net.minecraft.server.v1_6_R2.EntityCreeper;
import net.minecraft.server.v1_6_R2.EntityEnderDragon;
import net.minecraft.server.v1_6_R2.EntityGhast;
import net.minecraft.server.v1_6_R2.EntityMonster;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.EntitySlime;
import net.minecraft.server.v1_6_R2.EntityWither;
import net.minecraft.server.v1_6_R2.EntityWolf;
import net.minecraft.server.v1_6_R2.EntityZombie;
import net.minecraft.server.v1_6_R2.Item;
import net.minecraft.server.v1_6_R2.ItemStack;
import net.minecraft.server.v1_6_R2.NBTTagCompound;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class Utils {
	public static int FactionCheck(Entity entity, Town faction) {
		if (entity == null || faction == null) {
			return 0;
		}
		if (entity instanceof EntityPlayer) {
			Player player = ((EntityPlayer)entity).getBukkitEntity();
			if (player.getGameMode() == GameMode.CREATIVE) return 1;
			try {
				Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
				if (!resident.hasTown()) return 0;
				if (resident.getTown().equals(faction)) return 1;
				if (!resident.hasNation()) return 0;
				if (resident.getTown().getNation().hasTown(faction)) return 1;
				if (resident.getTown().getNation().hasAlly(faction.getNation())) return 1;
				if (resident.getTown().getNation().hasEnemy(faction.getNation())) return -1;
				return 0;
			} catch (Exception ex) {
				return 0;
			}
		} else if (entity instanceof FactionMob) {
			FactionMob fmob = (FactionMob) entity;
			try {
				if (fmob.getFaction() == null) return 0;
				if (fmob.getFaction().equals(faction)) return 1;
				if (!fmob.getFaction().hasNation()) return 0;
				if (fmob.getFaction().getNation().hasTown(faction)) return 1;
				if (fmob.getFaction().getNation().hasAlly(faction.getNation())) return 1;
				if (fmob.getFaction().getNation().hasEnemy(faction.getNation())) return -1;
				return 0;
			} catch (Exception ex) {
				return 0;
			}
		} else if (entity instanceof EntityWolf) {
			EntityWolf wolf = (EntityWolf) entity;
			if (wolf.isTamed()) {
				if (wolf.getOwner() != null) {
					return FactionCheck(wolf.getOwner(), faction);
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
		} else if (!FactionMobs.attackMobs) {
			return 0;
		} else if (entity instanceof EntityAnimal) {
			return 0;
		} else if (entity instanceof EntityZombie) {
			if (FactionMobs.attackZombies) {
				return -1;
			}
			return 0;
		} else if (entity instanceof EntityMonster
				|| entity instanceof EntityGhast
				|| entity instanceof EntityEnderDragon
				|| entity instanceof EntityWither) {
			return -1;
		} else if (entity instanceof EntitySlime) {
			EntitySlime slime = (EntitySlime) entity;
			if (slime.getSize() > 1) {
				return -1;
			} else {
				return 0;
			}
		}
		return 0;
	}
	
	public static void giveColorArmor(FactionMob entity) {
		int color = -1;
		if (entity.getFaction() == null) {
			return;
		} else if (FactionMobs.factionColors.containsKey(entity.getFaction().getName())) {
			color = FactionMobs.factionColors.get(entity.getFaction().getName());
		} else {
			FactionMobs.factionColors.put(entity.getFaction().getName(), 10511680);
		}
		
		if (color == -1 || color == 10511680) {
			entity.setEquipment(1, new ItemStack(Item.LEATHER_BOOTS));
			entity.setEquipment(2, new ItemStack(Item.LEATHER_LEGGINGS));
			entity.setEquipment(3, new ItemStack(Item.LEATHER_CHESTPLATE));
			entity.setEquipment(4, new ItemStack(Item.LEATHER_HELMET));
			return;
		}
		
		ItemStack[] itemStacks = {
				new ItemStack(Item.LEATHER_BOOTS), 
				new ItemStack(Item.LEATHER_LEGGINGS), 
				new ItemStack(Item.LEATHER_CHESTPLATE),
				new ItemStack(Item.LEATHER_HELMET)};

	    for (ItemStack i : itemStacks) {
	    	NBTTagCompound n = i.getTag();
		    if (n == null) {
		      n = new NBTTagCompound();
		      i.setTag(n);
		    }
		    NBTTagCompound n2 = n.getCompound("display");
		    if (!n.hasKey("display")) n.setCompound("display", n2);
		    n2.setInt("color", color);
	    }
	    
        entity.setEquipment(1, itemStacks[0]);
        entity.setEquipment(2, itemStacks[1]);
        entity.setEquipment(3, itemStacks[2]);
        entity.setEquipment(4, itemStacks[3]);
        return;
	}
	
	public FactionMob mobCreate() { // not implemented yet
		return null;
	}
	
	public static double dist3D(double x1, double x2, double y1, double y2, double z1, double z2) {
		return Math.sqrt(Math.pow(x1-x2,2) + Math.pow(y1-y2,2) + Math.pow(z1-z2,2));
	}
	
	public static int countMobsInFaction(Town faction) {
		int count = 0;
		for (FactionMob fmob : FactionMobs.mobList) {
			if (fmob.getFactionName().equals(faction.getName())) {
				count++;
			}
		}
		return count;
	}
}
