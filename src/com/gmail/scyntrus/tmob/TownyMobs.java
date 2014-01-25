package com.gmail.scyntrus.tmob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_7_R1.Entity;
import net.minecraft.server.v1_7_R1.EntityTypes;
import net.minecraft.util.org.apache.commons.io.IOUtils;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import com.gmail.scyntrus.tmob.mobs.Archer;
import com.gmail.scyntrus.tmob.mobs.Mage;
import com.gmail.scyntrus.tmob.mobs.Swordsman;
import com.gmail.scyntrus.tmob.mobs.Titan;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class TownyMobs extends JavaPlugin {
	
	public PluginManager pm = null;
	public static List<TownyMob> mobList = new ArrayList<TownyMob>();
	public static Map<String,Integer> townColors = new HashMap<String,Integer>();
	
	public Map<String,Boolean> mobLeader = new HashMap<String,Boolean>();
	
	public Map<String,List<TownyMob>> playerSelections = new HashMap<String,List<TownyMob>>();
	
	public static long mobCount = 0;
	
	public static String sndBreath = "";
	public static String sndHurt = "";
	public static String sndDeath = "";
	public static String sndStep = "";
	
	public static int spawnLimit = 50;
	public static int mobsPerTown = 0;
	public static boolean attackMobs = true;
	public static boolean noFriendlyFire = false;
	public static boolean noPlayerFriendlyFire = false;
	public static boolean displayMobTown = true;
	public static boolean attackZombies = true;
	public static boolean alertAllies = true;
	
	private long saveInterval = 6000;
	
    public Economy econ = null;
	public Boolean vaultEnabled = false;
	
	public static double mobSpeed = .3;
	public static double mobPatrolSpeed = .175;
	public static double mobNavRange = 64;
	
	public static TownyMobs instance;
	
	public static boolean scheduleChunkMobLoad = false;
	public static int chunkMobLoadTask = -1;
	
	public static boolean feedEnabled = true;
	public static float feedAmount = 5;
	
	@SuppressWarnings("rawtypes")
	private Map mapC;
	@SuppressWarnings("rawtypes")
	private Map mapD;
	@SuppressWarnings("rawtypes")
	private Map mapF;
	@SuppressWarnings("rawtypes")
	private Map mapG;
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void onEnable() {
		TownyMobs.instance = this;
		this.saveDefaultConfig();
		FileConfiguration config = this.getConfig();
		config.options().copyDefaults(true);
    	this.saveConfig();

		
		try {
			File defaultConfig = new File(this.getDataFolder(), "configDefaults.yml");
			defaultConfig.createNewFile();
			InputStream is = getClass().getResourceAsStream("/config.yml");
			FileOutputStream os = new FileOutputStream(defaultConfig);
			IOUtils.copy(is, os);
			is.close();
			os.close();
		} catch (Exception e) {
    	    System.out.println("[TownyMobs] Could not create default config file.");
		}
		
    	try {
    	    Class.forName("org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity");
    	} catch(Exception e) {
    	    System.out.println("[TownyMobs] You are running an unsupported version of CraftBukkit (requires v1_7_R1). TownyMobs will not be enabled.");
    	    this.getCommand("tm").setExecutor(new ErrorCommand(this));
    	    this.getCommand("tmc").setExecutor(new ErrorCommand(this));
    	    return;
    	}
    	
		int modelNum = 51;
		switch (config.getInt("model")) {
		case 0: // skeleton
			modelNum = 51;
			//TownyMobs.sndBreath = "mob.skeleton.say";
			TownyMobs.sndHurt = "mob.skeleton.hurt";
			TownyMobs.sndDeath = "mob.skeleton.death";
			TownyMobs.sndStep = "mob.skeleton.step";
			break;
		case 1: // zombie
			modelNum = 54;
			//TownyMobs.sndBreath = "mob.zombie.say";
			TownyMobs.sndHurt = "mob.zombie.hurt";
			TownyMobs.sndDeath = "mob.zombie.death";
			TownyMobs.sndStep = "mob.zombie.step";
			break;
		case 2: // pigzombie
			modelNum = 57;
			//TownyMobs.sndBreath = "mob.zombiepig.zpig";
			TownyMobs.sndHurt = "mob.zombiepig.zpighurt";
			TownyMobs.sndDeath = "mmob.zombiepig.zpigdeath";
			TownyMobs.sndStep = "mob.zombie.step";
			break;
		}

		TownyMobs.spawnLimit = config.getInt("spawnLimit", TownyMobs.spawnLimit);
		TownyMobs.mobsPerTown = config.getInt("mobsPerTown", TownyMobs.mobsPerTown);
		TownyMobs.noFriendlyFire = config.getBoolean("noFriendlyFire", TownyMobs.noFriendlyFire);
		TownyMobs.noPlayerFriendlyFire = config.getBoolean("noPlayerFriendlyFire", TownyMobs.noPlayerFriendlyFire);
		TownyMobs.alertAllies = config.getBoolean("alertAllies", TownyMobs.alertAllies);
		TownyMobs.displayMobTown = config.getBoolean("displayMobTown", TownyMobs.displayMobTown);
		TownyMobs.attackMobs = config.getBoolean("attackMobs", TownyMobs.attackMobs);
		TownyMobs.attackZombies = config.getBoolean("attackZombies", TownyMobs.attackZombies);
		TownyMobs.mobSpeed = (float) config.getDouble("mobSpeed", TownyMobs.mobSpeed);
		TownyMobs.mobPatrolSpeed = (float) config.getDouble("mobPatrolSpeed", TownyMobs.mobPatrolSpeed);
		TownyMobs.mobPatrolSpeed = TownyMobs.mobPatrolSpeed / TownyMobs.mobSpeed;
		TownyMobs.mobNavRange = (float) config.getDouble("mobNavRange", TownyMobs.mobNavRange);

		TownyMobs.feedEnabled = config.getBoolean("feedEnabled", TownyMobs.feedEnabled);
		TownyMobs.feedAmount = (float) config.getDouble("feedAmount", TownyMobs.feedAmount);
		
		Archer.maxHp = (float) config.getDouble("Archer.maxHp", Archer.maxHp);
		if (Archer.maxHp<1) Archer.maxHp = 1;
		Mage.maxHp = (float) config.getDouble("Mage.hp", Mage.maxHp);
		if (Mage.maxHp<1) Mage.maxHp = 1;
		Swordsman.maxHp = (float) config.getDouble("Swordsman.maxHp", Swordsman.maxHp);
		if (Swordsman.maxHp<1) Swordsman.maxHp = 1;
		Titan.maxHp = (float) config.getDouble("Titan.maxHp", Titan.maxHp);
		if (Titan.maxHp<1) Titan.maxHp = 1;
		
		Archer.damage = config.getDouble("Archer.damage", Archer.damage);
		if (Archer.damage<0) Archer.damage = 0;
		Swordsman.damage = config.getDouble("Swordsman.damage", Swordsman.damage);
		if (Swordsman.damage<0) Swordsman.damage = 0;
		Titan.damage = config.getDouble("Titan.damage", Titan.damage);
		if (Titan.damage<0) Titan.damage = 0;
		
		Archer.enabled = config.getBoolean("Archer.enabled", Archer.enabled);
		Mage.enabled = config.getBoolean("Mage.enabled", Mage.enabled);
		Swordsman.enabled = config.getBoolean("Swordsman.enabled", Swordsman.enabled);
		Titan.enabled = config.getBoolean("Titan.enabled", Titan.enabled);
		
		Archer.powerCost = config.getDouble("Archer.powerCost", Archer.powerCost);
		Archer.moneyCost = config.getDouble("Archer.moneyCost", Archer.moneyCost);
		Mage.powerCost = config.getDouble("Mage.powerCost", Mage.powerCost);
		Mage.moneyCost = config.getDouble("Mage.moneyCost", Mage.moneyCost);
		Swordsman.powerCost = config.getDouble("Swordsman.powerCost", Swordsman.powerCost);
		Swordsman.moneyCost = config.getDouble("Swordsman.moneyCost", Swordsman.moneyCost);
		Titan.powerCost = config.getDouble("Titan.powerCost", Titan.powerCost);
		Titan.moneyCost = config.getDouble("Titan.moneyCost", Titan.moneyCost);

		Archer.drops = config.getInt("Archer.drops", 0);
		Mage.drops = config.getInt("Mage.drops", 0);
		Swordsman.drops = config.getInt("Swordsman.drops", 0);
		Titan.drops = config.getInt("Titan.drops", 0);
		
		this.pm = this.getServer().getPluginManager();
	    try {
	    	Field fieldC = EntityTypes.class.getDeclaredField("c");
	        fieldC.setAccessible(true);
	    	Field fieldD = EntityTypes.class.getDeclaredField("d");
	        fieldD.setAccessible(true);
	    	Field fieldF = EntityTypes.class.getDeclaredField("f");
	        fieldF.setAccessible(true);
	    	Field fieldG = EntityTypes.class.getDeclaredField("g");
	        fieldG.setAccessible(true);
	        
	        mapC = (Map) fieldC.get(null);
	        mapD = (Map) fieldD.get(null);
	        mapF = (Map) fieldF.get(null);
	        mapG = (Map) fieldG.get(null);
	    	
	    	addEntityType(Archer.class, Archer.typeName, modelNum);
	    	addEntityType(Swordsman.class, Swordsman.typeName, modelNum);
	    	addEntityType(Mage.class, Mage.typeName, modelNum);
	    	addEntityType(Titan.class, Titan.typeName, 99);
	    	
	    } catch (Exception e) {
        	this.getLogger().severe("[Fatal Error] Unable to register mobs");
	    	e.printStackTrace();
	    	pm.disablePlugin(this);
	    	return;
	    }
	    this.getCommand("tm").setExecutor(new TmCommand(this));
	    if (config.getBoolean("tmcEnabled", false)) {
		    this.getCommand("tmc").setExecutor(new TmcCommand(this));
	    }
	    
	    this.pm.registerEvents(new EntityListener(this), this);
	    this.pm.registerEvents(new CommandListener(this), this);
	    this.pm.registerEvents(new RenameListener(this), this);
	    
	    File colorFile = new File(getDataFolder(), "colors.dat");
	    if (colorFile.exists()){
			try {
				FileInputStream fileInputStream = new FileInputStream(colorFile);
		    	ObjectInputStream oInputStream = new ObjectInputStream(fileInputStream);
		    	TownyMobs.townColors = (HashMap<String, Integer>) oInputStream.readObject();
		    	oInputStream.close();
		    	fileInputStream.close();
			} catch (Exception e) {
	        	this.getLogger().severe("[TownyMobs] Error reading town colors file, colors.dat");
			}
	    }
	    
	    if (config.getBoolean("autoSave", false)) {
	    	this.saveInterval = config.getLong("saveInterval", this.saveInterval);
	    	if (this.saveInterval > 0) {
	    		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new AutoSaver(this), this.saveInterval, this.saveInterval);
	    		System.out.println("[TownyMobs] Auto-Save enabled.");
	    	}
	    }
	    
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                econ = rsp.getProvider();
                if (econ != null) {
                	vaultEnabled = true;
                }
            }
        }
        if (vaultEnabled) {
        	System.out.println("[TownyMobs] Vault detected.");
        } else {
        	System.out.println("[TownyMobs] Vault not detected.");
        }
        
		try { // using mcstats.org metrics
			MetricsLite metrics = new MetricsLite(this);
		    metrics.start();
		} catch (IOException e) {
            System.out.println("[Metrics] " + e.getMessage());
		}
        
		this.loadMobList();
		
        chunkMobLoadTask = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ChunkMobLoader(this), 4, 4);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addEntityType(Class paramClass, String paramString, int paramInt) {
	    mapC.put(paramString, paramClass);
	    mapD.put(paramClass, paramString);
	    mapF.put(paramClass, Integer.valueOf(paramInt));
	    mapG.put(paramString, Integer.valueOf(paramInt));
	}
	
	public void onDisable() {
		this.saveMobList();
	}
	
	public void loadMobList() {
		File file = new File(getDataFolder(), "data.dat");
	    boolean backup = false;
	    if (file.exists()) {
	    	YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
			@SuppressWarnings("unchecked")
			List<List<String>> save = (List<List<String>>) conf.getList("data", new ArrayList<List<String>>());
			for (List<String> mobData : save) {
				TownyMob newMob = null;
				if (mobData.size() < 10) {
					System.out.println("Incomplete Towny Mob found and removed. Did you delete or rename a world?");
					if (!backup) {
						backup = true;
						try {
							conf.save(new File(getDataFolder(), "data_backup.dat"));
							System.out.println("Backup file saved as data_backup.dat");
						} catch (IOException e) {
							System.out.println("Failed to save backup file");
						}
					}
					continue;
				}
				org.bukkit.World world = this.getServer().getWorld(mobData.get(1));
				if (world == null) {
					System.out.println("Worldless Towny Mob found and removed. Did you delete or rename a world?");
					if (!backup) {
						backup = true;
						try {
							conf.save(new File(getDataFolder(), "data_backup.dat"));
							System.out.println("Backup file saved as data_backup.dat");
						} catch (IOException e) {
							System.out.println("Failed to save backup file");
						}
					}
					continue;
				}
				Town town = null;
				try {
					town = TownyUniverse.getDataSource().getTown(mobData.get(2));
				} catch (Exception ex) {
					System.out.println("Townless Towny Mob found and removed. Did something happen to Towns?");
					if (!backup) {
						backup = true;
						try {
							conf.save(new File(getDataFolder(), "data_backup.dat"));
							System.out.println("Backup file saved as data_backup.dat");
						} catch (IOException e) {
							System.out.println("Failed to save backup file");
						}
					}
					continue;
				}
				if (town == null) {
					System.out.println("Townless Towny Mob found and removed. Did something happen to Towns?");
					if (!backup) {
						backup = true;
						try {
							conf.save(new File(getDataFolder(), "data_backup.dat"));
							System.out.println("Backup file saved as data_backup.dat");
						} catch (IOException e) {
							System.out.println("Failed to save backup file");
						}
					}
					continue;
				}
				Location spawnLoc = new Location(
						world, 
						Double.parseDouble(mobData.get(3)), 
						Double.parseDouble(mobData.get(4)), 
						Double.parseDouble(mobData.get(5)));
				if (mobData.get(0).equalsIgnoreCase("Archer") || mobData.get(0).equalsIgnoreCase("Ranger")) {
					newMob = new Archer(spawnLoc, town);
				} else if (mobData.get(0).equalsIgnoreCase("Mage")) {
					newMob = new Mage(spawnLoc, town);
				} else if (mobData.get(0).equalsIgnoreCase("Swordsman")) {
					newMob = new Swordsman(spawnLoc, town);
				} else if (mobData.get(0).equalsIgnoreCase("Titan")) {
					newMob = new Titan(spawnLoc, town);
				} else {
					continue;
				}
				if (newMob.getTown() == null || newMob.getTownName() == null) {
					System.out.println("Townless Towny Mob found and removed. Did something happen to Towns?");
					if (!backup) {
						backup = true;
						try {
							conf.save(new File(getDataFolder(), "data_backup.dat"));
							System.out.println("Backup file saved as data_backup.dat");
						} catch (IOException e) {
							System.out.println("Failed to save backup file");
						}
					}
					continue;
				}
				newMob.setPosition(Double.parseDouble(mobData.get(6)),
						Double.parseDouble(mobData.get(7)),
						Double.parseDouble(mobData.get(8)));
				newMob.setHealth(Float.parseFloat(mobData.get(9)));
				
				if (mobData.size() > 10) {
					newMob.setPoi(
						Double.parseDouble(mobData.get(10)), 
						Double.parseDouble(mobData.get(11)), 
						Double.parseDouble(mobData.get(12)));
					newMob.setOrder(mobData.get(13));
				} else {
					newMob.setPoi(
							Double.parseDouble(mobData.get(6)), 
							Double.parseDouble(mobData.get(7)), 
							Double.parseDouble(mobData.get(8)));
					newMob.setOrder("poi");
				}
				
				if (!newMob.getEntity().world.addEntity((Entity) newMob, SpawnReason.CUSTOM)) {
					System.out.println("Unable to respawn a Towny Mob.");
					if (!backup) {
						backup = true;
						try {
							conf.save(new File(getDataFolder(), "data_backup.dat"));
							System.out.println("Backup file saved as data_backup.dat");
						} catch (IOException e) {
							System.out.println("Failed to save backup file");
						}
					}
				}
				mobList.add(newMob);
				newMob.getEntity().dead = false;
			}
	    }
	}
	
	public void saveMobList() {
		YamlConfiguration conf = new YamlConfiguration();
		List<List<String>> save = new ArrayList<List<String>>();
		for (TownyMob tmob : mobList) {
			if (tmob.getTown() == null) {
				continue;
			}
			List<String> mobData = new ArrayList<String>();
			mobData.add(tmob.getTypeName()); //0
			Location spawnLoc = tmob.getSpawn();
			mobData.add(spawnLoc.getWorld().getName()); //1
			mobData.add(tmob.getTownName()); //2
			mobData.add(""+spawnLoc.getX()); //3
			mobData.add(""+spawnLoc.getY());
			mobData.add(""+spawnLoc.getZ());
			mobData.add(""+tmob.getlocX()); //6
			mobData.add(""+tmob.getlocY());
			mobData.add(""+tmob.getlocZ());
			mobData.add(""+tmob.getHealth()); //9
			mobData.add(""+tmob.getPoiX()); //10
			mobData.add(""+tmob.getPoiY());
			mobData.add(""+tmob.getPoiZ());
			mobData.add(tmob.getOrder()); //13
			save.add(mobData);
		}
		conf.set("data", save);
		try {
			conf.save(new File(getDataFolder(), "data.dat"));
			System.out.println("TownyMobs data saved.");
		} catch (IOException e) {
        	this.getLogger().severe("Failed to save TownyMob data, data.dat");
		}
		try {
		    File colorFile = new File(getDataFolder(), "colors.dat");
		    colorFile.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(colorFile);
	    	ObjectOutputStream oOut = new ObjectOutputStream(fileOut);
	    	oOut.writeObject(TownyMobs.townColors);
	    	oOut.close();
	    	fileOut.close();
			System.out.println("TownyMobs color data saved.");
		} catch (Exception e) {
        	this.getLogger().severe("Error writing town colors file, colors.dat");
		}
	}
	
	public void updateList() {
		for (int i = mobList.size()-1; i >= 0; i--) {
			mobList.get(i).updateMob();
		}
	}
	
	public static final String signature_Author = "Scyntrus";
	public static final String signature_URL = "http://dev.bukkit.org/bukkit-plugins/towny-mobs/";
	public static final String signature_Source = "http://github.com/Scyntrus/TownyMobs";
}
