package com.gmail.scyntrus.tmob.mobs;

import net.minecraft.server.v1_7_R4.AttributeInstance;
import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.DamageSource;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.EntityLiving;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.EntityPotion;
import net.minecraft.server.v1_7_R4.EntityProjectile;
import net.minecraft.server.v1_7_R4.EntityWitch;
import net.minecraft.server.v1_7_R4.EnumMonsterType;
import net.minecraft.server.v1_7_R4.GenericAttributes;
import net.minecraft.server.v1_7_R4.Item;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.MathHelper;
import net.minecraft.server.v1_7_R4.MobEffectList;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.PathfinderGoal;
import net.minecraft.server.v1_7_R4.PathfinderGoalArrowAttack;
import net.minecraft.server.v1_7_R4.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R4.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_7_R4.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R4.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R4.util.UnsafeList;
import org.bukkit.metadata.FixedMetadataValue;

import com.gmail.scyntrus.tmob.ReflectionManager;
import com.gmail.scyntrus.tmob.TownyMob;
import com.gmail.scyntrus.tmob.TownyMobs;
import com.gmail.scyntrus.tmob.Utils;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class Mage extends EntityWitch implements TownyMob {
	
	public Location spawnLoc = null;
	public Town town = null;
	public String townName = "";
	public Entity attackedBy = null;
	public static String typeName = "Mage";
	public static float maxHp = 20;
	public static Boolean enabled = true;
	public static double powerCost = 0;
	public static double moneyCost = 0;
	public static double range = 16;
	public static int drops = 0;
	private int retargetTime = 0;
	private double moveSpeed;
	
	public double poiX=0, poiY=0, poiZ=0;
	public String order = "poi";
	
	public Mage(World world) {
		super(world);
		this.forceDie();
	}
	
	public Mage(Location spawnLoc, Town town) {
		super(((CraftWorld) spawnLoc.getWorld()).getHandle());
		this.setSpawn(spawnLoc);
		this.setTown(town);
		Utils.giveColorArmor(this);
	    this.persistent = true;
	    this.fireProof = false;
	    this.canPickUpLoot = false;
	    this.moveSpeed = TownyMobs.mobSpeed;
	    getAttributeInstance(GenericAttributes.d).setValue(this.moveSpeed);
	    getAttributeInstance(GenericAttributes.maxHealth).setValue(maxHp);
	    this.setHealth(maxHp);
	    this.W = 1.5F;
	    this.getNavigation().a(false);
	    this.getNavigation().b(false);
	    this.getNavigation().c(true);
	    this.getNavigation().d(false);
	    this.getNavigation().e(true);
		this.setEquipment(0, new ItemStack((Item)Item.REGISTRY.get("potion"), 1, 8204));
	    
	    if (ReflectionManager.goodNavigationE) {
		    try {
				AttributeInstance e = (AttributeInstance) ReflectionManager.navigationE.get(this.getNavigation());
				e.setValue(TownyMobs.mobNavRange);
			} catch (Exception e) {
			}
	    }
	    if (ReflectionManager.goodPathfinderGoalSelectorB) {
		    try {
		    	ReflectionManager.pathfinderGoalSelectorB.set(this.goalSelector, new UnsafeList<PathfinderGoal>());
		    	ReflectionManager.pathfinderGoalSelectorB.set(this.targetSelector, new UnsafeList<PathfinderGoal>());
		    } catch (Exception e) {
			}
	    }
	    
	    this.goalSelector.a(1, new PathfinderGoalFloat(this));
	    this.goalSelector.a(2, new PathfinderGoalArrowAttack(this, 1.0, 60, 10.0F));
	    this.goalSelector.a(2, new PathfinderGoalRandomStroll(this, 1.0));
	    this.goalSelector.a(3, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
	    this.goalSelector.a(3, new PathfinderGoalRandomLookaround(this));
	    this.getBukkitEntity().setMetadata("CustomEntity", new FixedMetadataValue(TownyMobs.instance, true));
	    this.getBukkitEntity().setMetadata("TownyMob", new FixedMetadataValue(TownyMobs.instance, true));
	}

	@Override
	public void e() {
		super.e();
		if (--retargetTime < 0) {
			retargetTime = 20;
			if (this.getGoalTarget() == null || !this.getGoalTarget().isAlive()) {
				this.findTarget();
			} else {
				double dist = Utils.dist3D(this.locX, this.getGoalTarget().locX, this.locY, this.getGoalTarget().locY, this.locZ, this.getGoalTarget().locZ);
				if (dist > range) {
					this.findTarget();
				} else if (dist > 1.5) {
					this.findCloserTarget();
				}
			}
			if (this.getGoalTarget() == null) {
				if (this.order == null || this.order.equals("home") || this.order.equals("")) {
					this.getNavigation().a(this.spawnLoc.getX(), this.spawnLoc.getY(), this.spawnLoc.getZ(), 1.0);
					this.order = "home";
					return;
				} else if (this.order.equals("poi")) {
					this.getNavigation().a(this.poiX, this.poiY, this.poiZ, 1.0);
					return;
				} else if (this.order.equals("wander")) {
					return;
				} else if (this.order.equals("phome")) {
					this.getNavigation().a(this.spawnLoc.getX(), this.spawnLoc.getY(), this.spawnLoc.getZ(), TownyMobs.mobPatrolSpeed);
					if (Utils.dist3D(this.locX,this.spawnLoc.getX(),this.locY,this.spawnLoc.getY(),this.locZ,this.spawnLoc.getZ()) < 1) {
						this.order = "ppoi";
					}
					return;
				} else if (this.order.equals("ppoi")) {
					this.getNavigation().a(poiX, poiY, poiZ, TownyMobs.mobPatrolSpeed);
					if (Utils.dist3D(this.locX,this.poiX,this.locY,this.poiY,this.locZ,this.poiZ) < 1) {
						this.order = "phome";
					}
					return;
				} else if (this.order.equals("path")) {
					this.getNavigation().a(poiX, poiY, poiZ, 1.0);
					if (Utils.dist3D(this.locX,this.poiX,this.locY,this.poiY,this.locZ,this.poiZ) < 1) {
						this.order = "home";
					}
					return;
				}
			}
		}
		return;
	}
	
	private void setSpawn(Location loc) {
		spawnLoc = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
		this.setPosition(loc.getX(), loc.getY(), loc.getZ());
		this.setPoi(loc.getX(),loc.getY(),loc.getZ());
		this.order = "home";
	}
	
	public Entity findCloserTarget() {
		if (this.attackedBy != null) {
			if (this.attackedBy.isAlive() 
					&& this.attackedBy.world.getWorldData().getName().equals(this.world.getWorldData().getName())
					&& Utils.TownCheck(this.attackedBy, this.town) < 1) {
				double dist = Utils.dist3D(this.locX, this.attackedBy.locX, this.locY, this.attackedBy.locY, this.locZ, this.attackedBy.locZ);
				if (dist < 16) {
					this.setTarget(this.attackedBy);
					return this.attackedBy;
				} else if (dist > 32) {
					this.attackedBy = null;
				}
			} else {
				this.attackedBy = null;
			}
		}
		Location thisLoc;
		double thisDist;
		for (org.bukkit.entity.Entity e : this.getBukkitEntity().getNearbyEntities(1.5, 1.5, 1.5)) {
			if (!e.isDead() && e instanceof CraftLivingEntity && Utils.TownCheck(((CraftEntity) e).getHandle(), town) == -1) {
				thisLoc = e.getLocation();
				thisDist = Math.sqrt(Math.pow(this.locX-thisLoc.getX(),2) + Math.pow(this.locY-thisLoc.getY(),2) + Math.pow(this.locZ-thisLoc.getZ(),2));
				if (thisDist < 1.5) {
					if (((CraftLivingEntity) this.getBukkitEntity()).hasLineOfSight(e)) {
						this.setTarget(((CraftEntity) e).getHandle());
						return ((CraftEntity) e).getHandle();
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public Entity findTarget() {
		Entity found = this.findCloserTarget();
		if (found != null) {
			return found;
		}
		double dist = range;
		Location thisLoc;
		double thisDist;
		for (org.bukkit.entity.Entity e : this.getBukkitEntity().getNearbyEntities(range, range, range)) {
			if (!e.isDead() && e instanceof CraftLivingEntity && Utils.TownCheck(((CraftEntity) e).getHandle(), town) == -1) {
				thisLoc = e.getLocation();
				thisDist = Math.sqrt(Math.pow(this.locX-thisLoc.getX(),2) + Math.pow(this.locY-thisLoc.getY(),2) + Math.pow(this.locZ-thisLoc.getZ(),2));
				if (thisDist < dist) {
					if (((CraftLivingEntity) this.getBukkitEntity()).hasLineOfSight(e)) {
						found = ((CraftEntity) e).getHandle();
						dist = thisDist;
					}
				}
			}
		}
		this.setTarget(found);
		return found;
	}
	
	@Override
	public boolean damageEntity(DamageSource damagesource, float i) {
		boolean out = super.damageEntity(damagesource, i);
		if (out) {
			switch (Utils.TownCheck(damagesource.getEntity(), this.town)) {
			case 1:
				this.findTarget();
				if (damagesource.getEntity() instanceof EntityPlayer) {
					this.lastDamageByPlayerTime = 0;
				}
				break;
			case 0:
			case -1:
				if (damagesource.getEntity() instanceof EntityLiving) {
					this.attackedBy = damagesource.getEntity();
					this.setTarget(damagesource.getEntity());
				} else if (damagesource.getEntity() instanceof EntityProjectile) {
					EntityProjectile p = (EntityProjectile) damagesource.getEntity();
					this.attackedBy = p.getShooter();
					this.setTarget(p.getShooter());
				} else {
					this.findTarget();
				}
				break;
			}
		}
		return out;
	}
	
	@Override
	public boolean canSpawn() {
		return true;
	}

	@Override
	public Town getTown() {
		if (this.town == null) {
			try {
				this.setTown(TownyUniverse.getDataSource().getTown(this.townName));
			} catch (NotRegisteredException e) {
				this.forceDie();
			}
		}
		if (this.town == null) {
			this.forceDie();
			System.out.println("[Error] Found and removed townless town mob");
		}
		return this.town;
	}

	public void setTown(Town town) {
		this.town = town;
		if (town == null) forceDie();
		this.townName = new String(town.getName());
		if (TownyMobs.displayMobTown) {
			this.setCustomName(ChatColor.YELLOW + this.townName + " " + typeName);
			this.setCustomNameVisible(true);
		}
	}
	
	@Override
	public void setTarget(Entity entity) {
		this.target = entity;
		if (entity instanceof EntityLiving) {
			this.setGoalTarget((EntityLiving) entity);
		} else if (entity == null) {
			this.setGoalTarget(null);
		}
		if (this.getGoalTarget() != null && !this.getGoalTarget().isAlive()) {
			this.setGoalTarget(null);
		}
	}
	
	@Override
	public void setGoalTarget(EntityLiving target) {
		if (this.target instanceof EntityLiving && this.target.isAlive()) {
			super.setGoalTarget((EntityLiving) this.target);
		} else {
			super.setGoalTarget(null);
		}
	}
	
	@Override
	public void updateMob() {
		try {
			this.setTown(TownyUniverse.getDataSource().getTown(this.townName));
		} catch (NotRegisteredException e) {
			this.forceDie();
			return;
		}
		if (this.town == null) {
			this.forceDie();
			return;
		}
		if (this.target instanceof EntityLiving && this.target.isAlive()) {
			super.setGoalTarget((EntityLiving) this.target);
		} else {
			this.findTarget();
		}
		Utils.giveColorArmor(this);
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public Location getSpawn() {
		return this.spawnLoc;
	}

	@Override
	public double getlocX() {
		return this.locX;
	}

	@Override
	public double getlocY() {
		return this.locY;
	}

	@Override
	public double getlocZ() {
		return this.locZ;
	}

	@Override
	protected String t() {
	    return TownyMobs.sndBreath;
	}

	@Override
	protected String aT() {
	    return TownyMobs.sndHurt;
	}

	@Override
	protected String aU() {
	    return TownyMobs.sndDeath;
	}

	@Override
	protected void a(int i, int j, int k, Block block) {
	    makeSound(TownyMobs.sndStep, 0.15F, 1.0F);
	}

	@Override
	public Boolean getEnabled() {
		return enabled;
	}

	@Override
	public double getPowerCost() {
		return powerCost;
	}

	@Override
	public double getMoneyCost() {
		return moneyCost;
	}
	
	@Override
	public boolean isTypeNotPersistent() {
		return false;
	}
	
	@Override
	public double getPoiX() {
		return this.poiX;
	}
	
	@Override
	public double getPoiY() {
		return this.poiY;
	}
	
	@Override
	public double getPoiZ() {
		return this.poiZ;
	}
	
	@Override
	public void setOrder(String order) {
		this.order = order;
	}
	
	@Override
	public void setPoi(double x, double y, double z) {
		this.poiX = x;
		this.poiY = y;
		this.poiZ = z;
	}
	
	@Override
	public String getOrder() {
		return this.order;
	}
	
	@Override
	public EntityLiving getEntity() {
		return this;
	}
	
	@Override
	public String getTownName() {
		if (this.townName == null) this.townName = "";
		return this.townName;
	}
	
	@Override
	public void die() {
		if (this.getHealth() <= 0) {
    		super.die();
    		this.setHealth(0);
    		this.setEquipment(0, null);
    		this.setEquipment(1, null);
    		this.setEquipment(2, null);
    		this.setEquipment(3, null);
    		this.setEquipment(4, null);
    		if (TownyMobs.mobList.contains(this)) {
    			TownyMobs.mobList.remove(this);
    		}
		}
	}
	
	@Override
	public void forceDie() {
		this.setHealth(0);
		this.die();
	}

	@Override
	public boolean c(NBTTagCompound nbttagcompound) {
		return false;
	}

	@Override
	public boolean d(NBTTagCompound nbttagcompound) {
		return false;
	}
	
	@Override
	public void f(NBTTagCompound nbttagcompound) {
		this.forceDie();
	}

	@Override
	public void clearAttackedBy() {
		if (this.target == this.attackedBy) {
			this.setTarget(null);
		}
		this.attackedBy = null;
	}
	
	@Override
	public EnumMonsterType getMonsterType() {
		return EnumMonsterType.UNDEFINED;
	}

	@Override
	public int getDrops() {
		return drops;
	}
	
	@Override
	public boolean softAgro(Entity entity) {
		if (this.attackedBy == null 
				&& entity instanceof EntityLiving
				&& entity.isAlive()) {
			this.attackedBy = entity;
			this.setTarget(entity);
			return true;
		}
		return false;
	}
	
	@Override
	public void setHealth(float f) {
		this.datawatcher.watch(6, Float.valueOf(MathHelper.a(f, 0.0F, maxHp)));
	}
	
	@Override
	public void h() {
		if (this.getHealth() > 0) {
			this.dead = false;
		}
		this.an = false;
		super.h();
	}

    public void a(EntityLiving paramEntityLiving, float paramFloat) {
        if (bZ())
            return;

        EntityPotion localEntityPotion = new EntityPotion(this.world, this, 32732);
        localEntityPotion.pitch -= -20.0F;
        double d1 = paramEntityLiving.locX + paramEntityLiving.motX - this.locX;
        double d2 = paramEntityLiving.locY + paramEntityLiving.getHeadHeight() - 1.100000023841858D - this.locY;
        double d3 = paramEntityLiving.locZ + paramEntityLiving.motZ - this.locZ;
        float f = MathHelper.sqrt(d1 * d1 + d3 * d3);

        if ((f >= 8.0F) && (!paramEntityLiving.hasEffect(MobEffectList.SLOWER_MOVEMENT)))
            localEntityPotion.setPotionValue(32698);
        else if ((paramEntityLiving.getHealth() >= 8.0F) && (!paramEntityLiving.hasEffect(MobEffectList.POISON)) 
                && (paramEntityLiving.getMonsterType() != EnumMonsterType.UNDEAD)
                && (paramEntityLiving.getMonsterType() != EnumMonsterType.ARTHROPOD))
            localEntityPotion.setPotionValue(32660);
        else if ((f <= 3.0F) && (!paramEntityLiving.hasEffect(MobEffectList.WEAKNESS)) && (this.random.nextFloat() < 0.25F)) {
            localEntityPotion.setPotionValue(32696);
        }
        else if (paramEntityLiving.getMonsterType() == EnumMonsterType.UNDEAD) {
            localEntityPotion.setPotionValue(32696);
        }

        localEntityPotion.shoot(d1, d2 + f * 0.2F, d3, 0.75F, 8.0F);

        this.world.addEntity(localEntityPotion);
    }
}
