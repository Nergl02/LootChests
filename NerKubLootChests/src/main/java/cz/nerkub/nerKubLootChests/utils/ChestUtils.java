package cz.nerkub.nerKubLootChests.utils;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public class ChestUtils {

	public static String getChestNameAtLocation(Location loc) {
		var data = NerKubLootChests.getInstance().getChestData();
		if (!data.isConfigurationSection("chests")) return null;

		for (String chestName : data.getConfigurationSection("chests").getKeys(false)) {
			ConfigurationSection chest = data.getConfigurationSection("chests." + chestName);
			if (chest == null) continue;

			for (var locMap : chest.getMapList("locations")) {
				Location storedLoc = Location.deserialize((Map<String, Object>) locMap);
				if (storedLoc.getWorld().equals(loc.getWorld()) && storedLoc.getBlock().getLocation().equals(loc.getBlock().getLocation())) {
					return chestName;
				}

			}
		}

		return null;
	}
}
