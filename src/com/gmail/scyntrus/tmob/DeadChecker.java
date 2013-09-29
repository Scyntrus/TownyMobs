package com.gmail.scyntrus.tmob;

public class DeadChecker implements Runnable {

	TownyMobs plugin;
	
    public DeadChecker(TownyMobs townyMobs) {
		this.plugin = townyMobs;
	}

	public void run() {
		for (TownyMob tmob : TownyMobs.mobList) {
 			if (tmob.getEntity().dead && tmob.getHealth() > 0) {
 				tmob.getEntity().dead = false;
 				//tmob.getEntity().world.addEntity(tmob.getEntity(), SpawnReason.CUSTOM);
 			}
		}
    }
}
