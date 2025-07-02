package cz.nerkub.nerKubLootChests.managers;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.SupportedContainers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class HologramUpdater implements Runnable {

	@Override
	public void run() {
		var data = NerKubLootChests.getInstance().getChestData();
		ConfigurationSection chests = data.getConfigurationSection("chests");
		if (chests == null) return;

		long now = System.currentTimeMillis() / 1000;

		for (String chestName : chests.getKeys(false)) {
			ConfigurationSection chest = chests.getConfigurationSection(chestName);
			if (chest == null) continue;

			final String chestId = chestName;
			int refreshTime = chest.getInt("refreshTime", 300);
			long last = chest.getLong("lastRefreshed", 0);
			final int safeTimeLeft = Math.max(0, (int) (refreshTime - (now - last)));

			List<Map<?, ?>> locations = chest.getMapList("locations");
			if (locations == null || locations.isEmpty()) continue;

			for (Map<?, ?> locMap : locations) {
				Location loc = Location.deserialize((Map<String, Object>) locMap);
				if (loc.getWorld() == null) continue;

				Block block = loc.getBlock();
				if (!SupportedContainers.VALID_CONTAINERS.contains(block.getType())) continue;

				Bukkit.getScheduler().runTask(NerKubLootChests.getInstance(), () -> {
					if (!(block.getState() instanceof Container container)) return;

					Inventory inv = container.getInventory();
					long items = Arrays.stream(inv.getContents())
							.filter(Objects::nonNull)
							.filter(i -> i.getType() != Material.AIR)
							.count();

					if (items > 0) {
						HologramManager.createOrUpdateLootableHologram(chestId, loc, (int) items);
					} else {
						HologramManager.createOrUpdateCountdownHologram(chestId, loc, safeTimeLeft);
					}
				});
			}
		}
	}
}
