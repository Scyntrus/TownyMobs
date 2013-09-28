package com.gmail.scyntrus.tmob;

public class AutoSaver implements Runnable {

	TownyMobs plugin;
	
    public AutoSaver(TownyMobs townyMobs) {
		this.plugin = townyMobs;
	}

	public void run() {
		this.plugin.saveMobList();
		System.out.println("Towny Mobs data saved via AutoSave");
    }
}
