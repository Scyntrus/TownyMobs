package com.gmail.scyntrus.tmob;

import net.minecraft.server.v1_7_R1.WorldServer;

import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class ChunkMobLoader implements Runnable {

	TownyMobs plugin;
	
    public ChunkMobLoader(TownyMobs townyMobs) {
		this.plugin = townyMobs;
	}

	public void run() {
		if (TownyMobs.scheduleChunkMobLoad) {
			TownyMobs.scheduleChunkMobLoad = false;
			for (TownyMob tmob : TownyMobs.mobList) {
	 			if (!((WorldServer) tmob.getEntity().world).getTracker().trackedEntities.b(tmob.getEntity().getId())) {
					try	{
						tmob.getEntity().world.addEntity(tmob.getEntity(), SpawnReason.CUSTOM);
					} catch (Exception ex) {}
					tmob.getEntity().dead = false;
	 			}
			}
		}
    }
}
