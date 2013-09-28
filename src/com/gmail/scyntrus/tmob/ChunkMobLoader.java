package com.gmail.scyntrus.tmob;

import net.minecraft.server.v1_6_R3.WorldServer;

import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class ChunkMobLoader implements Runnable {

	TownyMobs plugin;
	
    public ChunkMobLoader(TownyMobs townyMobs) {
		this.plugin = townyMobs;
	}

	public void run() {
		if (TownyMobs.scheduleChunkMobLoad) {
			TownyMobs.scheduleChunkMobLoad = false;
			for (TownyMob fmob : TownyMobs.mobList) {
	 			if (!((WorldServer) fmob.getEntity().world).getTracker().trackedEntities.b(fmob.getEntity().id)) {
					try	{
						fmob.getEntity().world.addEntity(fmob.getEntity(), SpawnReason.CUSTOM);
					} catch (Exception ex) {}
					fmob.getEntity().dead = false;
	 			}
			}
		}
    }
}
