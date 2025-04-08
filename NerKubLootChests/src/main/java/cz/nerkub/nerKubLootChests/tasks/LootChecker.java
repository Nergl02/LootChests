package cz.nerkub.nerKubLootChests.tasks;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.managers.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
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

			long last = chest.getLong("lastRefreshed", 0);
			int interval = chest.getInt("refreshTime", 300);

			List<Map<?, ?>> locations = chest.getMapList("locations");
			if (locations == null || locations.isEmpty()) continue;

			for (Map<?, ?> raw : locations) {
				Location loc = Location.deserialize((Map<String, Object>) raw);
				if (loc.getWorld() == null) continue;

				Bukkit.getScheduler().runTask(NerKubLootChests.getInstance(), () -> {
					Block block = loc.getBlock();
					if (block.getType() != Material.CHEST) return;

					Chest chestBlock = (Chest) block.getState();
					Inventory inv = chestBlock.getBlockInventory();

					boolean hasItems = Arrays.stream(inv.getContents())
							.filter(Objects::nonNull)
							.anyMatch(i -> i.getType() != Material.AIR);

					if (!hasItems && now - last < interval) {
						int timeLeft = (int) (interval - (now - last));
						HologramManager.spawnCountdown(loc, chestName, timeLeft);
					}
				});
			}
		}
	}
}
