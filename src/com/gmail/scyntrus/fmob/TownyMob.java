package com.gmail.scyntrus.fmob;

import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.ItemStack;

import org.bukkit.Location;

import com.palmergames.bukkit.towny.object.Town;

public interface TownyMob {
	public Town getFaction();
	public Location getSpawn();
	public void setPosition(double x, double y, double z);
	public double getlocX();
	public double getlocY();
	public double getlocZ();
	public Entity findTarget();
	public void updateMob();
	public String getTypeName();
	public boolean isAlive();
	public float getHealth();
	public void setHealth(float hp);
	public void die();
	public void setEquipment(int slot, ItemStack item);
	public Boolean getEnabled();
	public double getPowerCost();
	public double getMoneyCost();
	public EntityLiving getGoalTarget();
	public void setTarget(Entity entity);
	public double getPoiX();
	public double getPoiY();
	public double getPoiZ();
	public void setOrder(String order);
	public String getOrder();
	public void setPoi(double x, double y, double z);
	public EntityLiving getEntity();
	public String getFactionName();
	public void clearAttackedBy();
	public int getDrops();
	public boolean softAgro(Entity entity);
}
