package com.gmail.scyntrus.fmob;

public class AutoSaver implements Runnable {

	TownyMobs plugin;
	
    public AutoSaver(TownyMobs factionMobs) {
		this.plugin = factionMobs;
	}

	public void run() {
		this.plugin.saveMobList();
		System.out.println("Faction Mobs data saved via AutoSave");
    }
}
