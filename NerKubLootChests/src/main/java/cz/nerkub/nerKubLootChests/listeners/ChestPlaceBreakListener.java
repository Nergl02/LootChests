package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.managers.HologramManager;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.ChestUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ChestPlaceBreakListener implements Listener {

	private static final NamespacedKey CHEST_KEY =
			new NamespacedKey(NerKubLootChests.getInstance(), "lootchest");

	@EventHandler
	public void onChestPlace(BlockPlaceEvent event) {
		ItemStack item = event.getItemInHand();
		if (item == null || item.getType() != Material.CHEST) return;

		ItemMeta meta = item.getItemMeta();
		if (meta == null || !meta.getPersistentDataContainer().has(CHEST_KEY, PersistentDataType.STRING)) return;

		String chestName = meta.getPersistentDataContainer().get(CHEST_KEY, PersistentDataType.STRING);
		FileConfiguration data = NerKubLootChests.getInstance().getChestData();

		if (!data.contains("chests." + chestName)) {
			event.getPlayer().sendMessage(MessageManager.get("messages.not_registered", "name", chestName));
			return;
		}

		Location loc = event.getBlock().getLocation();
		Map<String, Object> serialized = loc.serialize();

		// Před přidáním nové lokaci se ujistíme, že nastavíme lastRefreshed
		serialized.put("lastRefreshed", System.currentTimeMillis() / 1000); // Nastavení času na aktuální čas v sekundách

		List<Map<?, ?>> locations = data.getMapList("chests." + chestName + ".locations");
		if (!locations.contains(serialized)) {
			locations.add(serialized);
			data.set("chests." + chestName + ".locations", locations);
			NerKubLootChests.getInstance().saveChestData();
		}

		// Zápis persistent dat do nové chestky
		Bukkit.getScheduler().runTaskLater(NerKubLootChests.getInstance(), () -> {
			if (loc.getBlock().getType() == Material.CHEST) {
				Chest placed = (Chest) loc.getBlock().getState();
				placed.getPersistentDataContainer().set(CHEST_KEY, PersistentDataType.STRING, chestName);
				placed.update();
			}
		}, 1L);

		String displayName = data.getString("chests." + chestName + ".displayName", chestName);
		int refreshTime = data.getInt("chests." + chestName + ".refreshTime", 300);

		HologramManager.createOrUpdateCountdownHologram(chestName, loc, refreshTime);
		event.getPlayer().sendMessage(MessageManager.get("messages.placed", "name", chestName));
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() != Material.CHEST) return;

		Block block = event.getBlock();
		Player player = event.getPlayer();

		if (!(block.getState() instanceof Chest chest)) return;
		Location loc1 = chest.getLocation();
		String chestName = ChestUtils.getChestNameAtLocation(loc1);
		if (chestName == null) {
			return;
		}

		if (!HologramManager.isFromPluginChest(chest)) return;

		if (chestName == null) return;

		// 🗂️ Odstranit lokaci z chests.yml
		YamlConfiguration data = NerKubLootChests.getInstance().getChestData();
		List<Map<?, ?>> locations = data.getMapList("chests." + chestName + ".locations");

		locations.removeIf(map -> {
			Location loc = Location.deserialize((Map<String, Object>) map);
			return loc.equals(chest.getLocation());
		});

		data.set("chests." + chestName + ".locations", locations);
		NerKubLootChests.getInstance().saveChestData();

		// 🧹 Odstranit hologram
		HologramManager.removeHologram(chest.getLocation());

		player.sendMessage(MessageManager.get("messages.broken", "name", chestName));
	}
}
