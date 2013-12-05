package com.gmail.scyntrus.tmob;

import org.bukkit.event.Listener;

import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.object.Town;

public class RenameListener implements Listener {
	
	TownyMobs plugin;
	
	public RenameListener(TownyMobs plugin) {
		this.plugin = plugin;
	}

	public void onTownRenameEvent(RenameTownEvent e) {
		String oldName = e.getOldName();
		Town town = e.getTown();
		for (TownyMob tmob : TownyMobs.mobList) {
			if (tmob.getTownName().equals(oldName)) {
				tmob.setTown(town);
			}
		}
	}
}
