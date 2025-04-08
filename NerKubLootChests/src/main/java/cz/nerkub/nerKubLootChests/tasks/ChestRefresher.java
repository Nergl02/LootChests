
package cz.nerkub.nerKubLootChests.tasks;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.managers.HologramManager;
import cz.nerkub.nerKubLootChests.utils.ItemSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ChestRefresher implements Runnable {

	@Override
	public void run() {
		var data = NerKubLootChests.getInstance().getChestData();
		if (!data.isConfigurationSection("chests")) return;

		long now = System.currentTimeMillis() / 1000;

		for (String chestName : data.getConfigurationSection("chests").getKeys(false)) {
			ConfigurationSection chest = data.getConfigurationSection("chests." + chestName);
			if (chest == null) continue;

			int refreshTime = chest.getInt("refreshTime", 300);
			int itemsPer = chest.getInt("itemsPerRefresh", 3);
			List<Map<?, ?>> originalLocations = chest.getMapList("locations");
			if (originalLocations == null || originalLocations.isEmpty()) continue;

			// 🎯 Nastavení raritních limitů
			Map<String, Integer> rarityLimits = new HashMap<>();
			ConfigurationSection limitsSec = chest.getConfigurationSection("rarityLimits");
			if (limitsSec != null) {
				for (String key : limitsSec.getKeys(false)) {
					rarityLimits.put(key.toUpperCase(), limitsSec.getInt(key, 0));
				}
			}

			List<Map<String, Object>> updatedLocations = new ArrayList<>();

			for (Map<?, ?> rawMap : originalLocations) {
				Map<String, Object> locMap = new HashMap<>();
				rawMap.forEach((k, v) -> locMap.put(k.toString(), v));
				Location loc = Location.deserialize(locMap);
				if (loc.getWorld() == null) continue;

				Object lastVal = locMap.get("lastRefreshed");
				long last = (lastVal instanceof Number) ? ((Number) lastVal).longValue() : -1L;
				long elapsed = (last > 0) ? (now - last) : 0;

				Bukkit.getScheduler().runTask(NerKubLootChests.getInstance(), () -> {
					Block block = loc.getBlock();
					if (block.getType() != Material.CHEST) return;

					Chest chestBlock = (Chest) block.getState();
					Inventory inv = chestBlock.getBlockInventory();

					boolean hasItems = Arrays.stream(inv.getContents())
							.filter(Objects::nonNull)
							.anyMatch(item -> item.getType() != Material.AIR);

					if (hasItems) {
						int count = (int) Arrays.stream(inv.getContents())
								.filter(Objects::nonNull)
								.filter(i -> i.getType() != Material.AIR)
								.count();
						HologramManager.createOrUpdateLootableHologram(chestName, loc, count);
						return;
					}

					if (last == -1L) {
						locMap.put("lastRefreshed", now);
						HologramManager.createOrUpdateCountdownHologram(chestName, loc, refreshTime);
						if (NerKubLootChests.getInstance().getConfig().getBoolean("debug")) {
							System.out.println("🕒 [Countdown Init] " + chestName + " at location: " + loc);
						}
						return;
					}

					if (elapsed >= refreshTime) {
						inv.clear();

						List<Map<?, ?>> pool = chest.getMapList("items");
						List<Map<?, ?>> shuffled = new ArrayList<>(pool);
						Collections.shuffle(shuffled);

						List<ItemStack> chosen = new ArrayList<>();
						Map<String, Integer> rarityCount = new HashMap<>();

						for (Map<?, ?> entry : shuffled) {
							int chance = entry.get("chance") instanceof Number ? ((Number) entry.get("chance")).intValue() : 100;
							String rarity = (entry.get("rarity") instanceof String) ? ((String) entry.get("rarity")).toUpperCase() : "COMMON";

							if (Math.random() * 100 < chance) {
								if (rarityLimits.containsKey(rarity)) {
									int current = rarityCount.getOrDefault(rarity, 0);
									if (current >= rarityLimits.get(rarity)) continue;
									rarityCount.put(rarity, current + 1);
								}
								String base64 = (String) entry.get("item");
								chosen.add(ItemSerializer.itemFromBase64(base64));
								if (chosen.size() >= itemsPer) break;
							}
						}

						// doplnit chybějící položky bez raritního omezení
						if (chosen.size() < itemsPer) {
							for (Map<?, ?> entry : shuffled) {
								String base64 = (String) entry.get("item");
								ItemStack item = ItemSerializer.itemFromBase64(base64);
								if (!chosen.contains(item)) {
									chosen.add(item);
									if (chosen.size() >= itemsPer) break;
								}
							}
						}

						Random rand = new Random();
						Set<Integer> used = new HashSet<>();
						for (ItemStack item : chosen) {
							int slot;
							do {
								slot = rand.nextInt(inv.getSize());
							} while (!used.add(slot));
							inv.setItem(slot, item);
						}

						locMap.put("world", loc.getWorld().getName());
						locMap.put("x", loc.getBlockX());
						locMap.put("y", loc.getBlockY());
						locMap.put("z", loc.getBlockZ());
						locMap.put("lastRefreshed", -1L);

						HologramManager.createOrUpdateLootableHologram(chestName, loc, chosen.size());
						if (NerKubLootChests.getInstance().getConfig().getBoolean("debug")) {
							System.out.println("✅ [Refresh] Chest '" + chestName + "' has been filled: " + loc);
						}
					} else {
						int timeLeft = (int) (refreshTime - elapsed);
						HologramManager.createOrUpdateCountdownHologram(chestName, loc, Math.max(timeLeft, 0));
					}
				});

				updatedLocations.add(locMap);
			}

			chest.set("locations", updatedLocations);
			NerKubLootChests.getInstance().saveChestData();
		}
	}
}
