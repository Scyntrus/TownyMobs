package com.gmail.scyntrus.fmob;

public class DeadChecker implements Runnable {

	TownyMobs plugin;
	
    public DeadChecker(TownyMobs factionMobs) {
		this.plugin = factionMobs;
	}

	public void run() {
		for (TownyMob fmob : TownyMobs.mobList) {
 			if (fmob.getEntity().dead && fmob.getHealth() > 0) {
 				fmob.getEntity().dead = false;
 				//fmob.getEntity().world.addEntity(fmob.getEntity(), SpawnReason.CUSTOM);
 			}
		}
    }
}
