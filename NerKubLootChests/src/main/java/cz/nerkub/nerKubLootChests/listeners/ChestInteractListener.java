package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.SupportedContainers;
import cz.nerkub.nerKubLootChests.managers.HologramManager;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ChestInteractListener implements Listener {

	private static final NamespacedKey CHEST_KEY =
			new NamespacedKey(NerKubLootChests.getInstance(), "lootchest");

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (!(event.getPlayer() instanceof Player player)) return;

		Inventory inv = event.getInventory();
		Block block = inv.getLocation() != null ? inv.getLocation().getBlock() : null;

		// Zajistíme, že blok existuje a je platný kontejner
		if (block == null || !SupportedContainers.VALID_CONTAINERS.contains(block.getType())) return;
		if (!(block.getState() instanceof Container container)) return;
		if (!(block.getState() instanceof TileState tileState)) return;

		// Má persistentní tag?
		if (!tileState.getPersistentDataContainer().has(CHEST_KEY, PersistentDataType.STRING)) return;
		String chestName = tileState.getPersistentDataContainer().get(CHEST_KEY, PersistentDataType.STRING);
		if (chestName == null) return;

		YamlConfiguration chestData = NerKubLootChests.getInstance().getChestData();
		if (!chestData.contains("chests." + chestName)) return;

		// 🧠 Odloženo kvůli async
		Bukkit.getScheduler().runTaskLater(NerKubLootChests.getInstance(), () -> {
			Inventory containerInv = container.getInventory();

			long now = System.currentTimeMillis() / 1000;
			int count = (int) Arrays.stream(containerInv.getContents())
					.filter(item -> item != null && item.getType() != Material.AIR)
					.count();

			// 🔁 Aktualizuj hologram
			HologramManager.createOrUpdateLootableHologram(chestName, container.getLocation(), count);

			// 💾 Pokud je prázdná, uložíme timestamp
			if (count == 0) {
				List<Map<?, ?>> list = chestData.getMapList("chests." + chestName + ".locations");
				List<Map<String, Object>> updated = new ArrayList<>();

				for (Map<?, ?> raw : list) {
					Map<String, Object> map = new HashMap<>();
					raw.forEach((k, v) -> map.put(k.toString(), v));

					Location entryLoc = Location.deserialize(map);
					if (entryLoc.equals(container.getLocation())) {
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
