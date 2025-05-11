package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.managers.HologramManager;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ChestInteractListener implements Listener {

	private static final NamespacedKey CHEST_KEY =
			new NamespacedKey(NerKubLootChests.getInstance(), "lootchest");

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) return;

		Inventory inv = player.getOpenInventory().getTopInventory();
		if (inv.getType() != org.bukkit.event.inventory.InventoryType.CHEST) return;

		// 🎯 Získání truhly podle mířeného bloku
		Block block = player.getTargetBlockExact(5);
		if (block == null || !(block.getState() instanceof Chest chest)) return;

		// ✅ Ověření, zda chestka má pluginový PersistentData tag
		var container = chest.getPersistentDataContainer();
		if (!container.has(CHEST_KEY, PersistentDataType.STRING)) return;

		String chestName = container.get(CHEST_KEY, PersistentDataType.STRING);
		if (chestName == null) return;

		YamlConfiguration chestData = NerKubLootChests.getInstance().getChestData();
		if (!chestData.contains("chests." + chestName)) return;

		Inventory chestInv = chest.getInventory();

		// 🧠 Odloženo o 1 tick kvůli async click eventu
		Bukkit.getScheduler().runTaskLater(NerKubLootChests.getInstance(), () -> {
			long now = System.currentTimeMillis() / 1000;
			int count = (int) Arrays.stream(chestInv.getContents())
					.filter(item -> item != null && item.getType() != Material.AIR)
					.count();

			// 🔁 Aktualizuj hologram
			HologramManager.createOrUpdateLootableHologram(chestName, chest.getLocation(), count);

			// 💾 Pokud je prázdná, uložíme timestamp
			if (count == 0) {
				List<Map<?, ?>> list = chestData.getMapList("chests." + chestName + ".locations");
				List<Map<String, Object>> updated = new ArrayList<>();

				for (Map<?, ?> raw : list) {
					Map<String, Object> map = new HashMap<>();
					raw.forEach((k, v) -> map.put(k.toString(), v));

					Location entryLoc = Location.deserialize(map);
					if (entryLoc.equals(chest.getLocation())) {
						map.put("lastLooted", now);
					}

					updated.add(map);
				}

				chestData.set("chests." + chestName + ".locations", updated);
				NerKubLootChests.getInstance().saveChestData();

				if (NerKubLootChests.getInstance().getConfig().getBoolean("debug")) {
					System.out.println("📦 [Loot] Chest '" + chestName + "' was looted – timestamp saved.");
				}
			}
		}, 1L);
	}
}
