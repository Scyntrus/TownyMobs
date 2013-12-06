package com.gmail.scyntrus.tmob;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.object.Town;

public class RenameListener implements Listener {
	
	TownyMobs plugin;
	
	public RenameListener(TownyMobs plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onTownRename(RenameTownEvent e) {
		String oldName = e.getOldName();
		Town town = e.getTown();
		TownyMobs.townColors.put(town.getName(), 
				TownyMobs.townColors.containsKey(oldName) ? 
						TownyMobs.townColors.remove(oldName) : 
							10511680);
		for (TownyMob tmob : TownyMobs.mobList) {
			if (tmob.getTownName().equals(oldName)) {
				tmob.setTown(town);
			}
		}
	}
}
