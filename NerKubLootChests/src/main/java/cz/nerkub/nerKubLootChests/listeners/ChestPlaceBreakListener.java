package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.SupportedContainers;
import cz.nerkub.nerKubLootChests.managers.HologramManager;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.ChestUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
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

import java.util.List;
import java.util.Map;

public class ChestPlaceBreakListener implements Listener {

	private static final NamespacedKey CHEST_KEY =
			new NamespacedKey(NerKubLootChests.getInstance(), "lootchest");

	@EventHandler
	public void onChestPlace(BlockPlaceEvent event) {
		ItemStack item = event.getItemInHand();
		if (item == null || !SupportedContainers.VALID_CONTAINERS.contains(item.getType())) return;

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
		serialized.put("lastRefreshed", System.currentTimeMillis() / 1000);

		List<Map<?, ?>> locations = data.getMapList("chests." + chestName + ".locations");
		if (!locations.contains(serialized)) {
			locations.add(serialized);
			data.set("chests." + chestName + ".locations", locations);
			NerKubLootChests.getInstance().saveChestData();
		}

		// Zápis persistent dat do nového blocku
		Bukkit.getScheduler().runTaskLater(NerKubLootChests.getInstance(), () -> {
			Block block = loc.getBlock();
			if (block.getState() instanceof TileState state) {
				state.getPersistentDataContainer().set(CHEST_KEY, PersistentDataType.STRING, chestName);
				state.update();
			}
		}, 1L);

		String displayName = data.getString("chests." + chestName + ".displayName", chestName);
		int refreshTime = data.getInt("chests." + chestName + ".refreshTime", 300);

		HologramManager.createOrUpdateCountdownHologram(chestName, loc, refreshTime);
		event.getPlayer().sendMessage(MessageManager.get("messages.placed", "name", chestName));
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();

		if (!SupportedContainers.VALID_CONTAINERS.contains(block.getType())) return;
		if (!(event.getPlayer() instanceof Player player)) {
			event.setCancelled(true);
			return;
		}

		if (!(block.getState() instanceof Container container)) return;

		Location loc = container.getLocation();
		String chestName = ChestUtils.getChestNameAtLocation(loc);

		if (chestName == null) return;

		// Zkontroluj, že ten block je opravdu lootchest (má persistent key)
		if (!(container instanceof TileState tileState)) return;
		if (!tileState.getPersistentDataContainer().has(CHEST_KEY, PersistentDataType.STRING)) return;

		if (!player.hasPermission("lootchest.admin")) {
			event.setCancelled(true);
			player.sendMessage(MessageManager.get("messages.no_permission"));
			return;
		}

		// Odstraň lokaci z configu
		YamlConfiguration data = NerKubLootChests.getInstance().getChestData();
		List<Map<?, ?>> locations = data.getMapList("chests." + chestName + ".locations");

		locations.removeIf(map -> {
			Location stored = Location.deserialize((Map<String, Object>) map);
			return stored.equals(loc);
		});

		data.set("chests." + chestName + ".locations", locations);
		NerKubLootChests.getInstance().saveChestData();

		// Odstranit hologram
		HologramManager.removeHologram(loc);

		player.sendMessage(MessageManager.get("messages.broken", "name", chestName));
	}
}
