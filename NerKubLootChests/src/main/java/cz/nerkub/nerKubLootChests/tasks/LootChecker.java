package cz.nerkub.nerKubLootChests.tasks;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.SupportedContainers;
import cz.nerkub.nerKubLootChests.managers.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class LootChecker implements Runnable {

	@Override
	public void run() {
		var data = NerKubLootChests.getInstance().getChestData();
		if (!data.isConfigurationSection("chests")) return;

		long now = System.currentTimeMillis() / 1000;

		for (String chestName : data.getConfigurationSection("chests").getKeys(false)) {
			ConfigurationSection chest = data.getConfigurationSection("chests." + chestName);
			if (chest == null) continue;

			int interval = chest.getInt("refreshTime", 300);
			List<Map<?, ?>> locations = chest.getMapList("locations");
			if (locations == null || locations.isEmpty()) continue;

			for (Map<?, ?> raw : locations) {
				Location loc = Location.deserialize((Map<String, Object>) raw);
				if (loc.getWorld() == null) continue;

				long lastRefreshed = 0;
				Object rawLast = raw.get("lastRefreshed");
				if (rawLast instanceof Number num) {
					lastRefreshed = num.longValue();
				}

				long finalLastRefreshed = lastRefreshed;

				Bukkit.getScheduler().runTask(NerKubLootChests.getInstance(), () -> {
					Block block = loc.getBlock();
					Material type = block.getType();

					if (!SupportedContainers.VALID_CONTAINERS.contains(type)) return;
					if (!(block.getState() instanceof Container container)) return;

					Inventory inv = container.getInventory();

					boolean hasItems = Arrays.stream(inv.getContents())
							.filter(Objects::nonNull)
							.anyMatch(item -> item.getType() != Material.AIR);

					if (!hasItems && now - finalLastRefreshed < interval) {
						int timeLeft = (int) (interval - (now - finalLastRefreshed));
						HologramManager.spawnCountdown(loc, chestName, timeLeft);
					}
				});
			}
		}
	}
}
