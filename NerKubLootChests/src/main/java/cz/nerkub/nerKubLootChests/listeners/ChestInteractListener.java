package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.managers.HologramManager;
import cz.nerkub.nerKubLootChests.utils.ChestUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.stream.Collectors;

public class ChestInteractListener implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;

		Inventory inv = event.getInventory();
		if (!(inv.getHolder() instanceof Chest chest)) return;

		Location loc = chest.getLocation();
		String chestName = ChestUtils.getChestNameAtLocation(loc);
		if (chestName == null) return;

		Bukkit.getScheduler().runTaskLater(NerKubLootChests.getInstance(), () -> {
			long now = System.currentTimeMillis() / 1000;
			int count = (int) Arrays.stream(inv.getContents())
					.filter(item -> item != null && item.getType() != Material.AIR)
					.count();

			// Update hologram
			HologramManager.createOrUpdateLootableHologram(chestName, loc, count);

			// Pokud je chestka prázdná → ulož nový lastLooted timestamp
			if (count == 0) {
				var chestData = NerKubLootChests.getInstance().getChestData();
				var list = chestData.getMapList("chests." + chestName + ".locations");
				List<Map<String, Object>> updated = new ArrayList<>();

				for (Map<?, ?> raw : list) {
					Map<String, Object> map = new HashMap<>();
					raw.forEach((k, v) -> map.put(k.toString(), v));

					Location entryLoc = Location.deserialize(map);
					if (entryLoc.getBlockX() == loc.getBlockX()
							&& entryLoc.getBlockY() == loc.getBlockY()
							&& entryLoc.getBlockZ() == loc.getBlockZ()
							&& entryLoc.getWorld().getName().equals(loc.getWorld().getName())) {
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
		}, 1L); // wait one tick after click
	}
}
