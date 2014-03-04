package com.gmail.scyntrus.tmob;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.object.Town;

public class TownyListener implements Listener {
	
	TownyMobs plugin;
	
	public TownyListener(TownyMobs plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTownRename(DeleteTownEvent e) {
		String townName = e.getTownName();
		for (int i = TownyMobs.mobList.size()-1; i >= 0; i--) {
			if (TownyMobs.mobList.get(i).getTownName().equals(townName)) {
				TownyMobs.mobList.get(i).die();
			}
		}
	}
}
