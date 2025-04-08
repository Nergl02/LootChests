package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.gui.EditChestMenu;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.ItemSerializer;
import cz.nerkub.nerKubLootChests.utils.TempItemCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AddLootItemsListener implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player player)) return;

		String rawTitle = ChatColor.stripColor(e.getView().getTitle());
		String expectedPrefix = ChatColor.stripColor(MessageManager.get("gui.add_items_title", "chest", "{chest}"))
				.replace("{chest}", "").trim();

		if (!rawTitle.startsWith(expectedPrefix)) return;

		// 🧠 Extrakce chestName z GUI titulu
		String chestName = rawTitle.substring(expectedPrefix.length()).trim().toLowerCase();
		if (chestName.isEmpty()) return;

		ItemStack clicked = e.getCurrentItem();
		if (clicked == null || clicked.getType() == Material.AIR) return;

		int confirmSlot = 6 * 9 - 6;
		int cancelSlot = 6 * 9 - 4;
		int clickedSlot = e.getSlot();

		Inventory inv = e.getInventory();

		if (clickedSlot != confirmSlot && clickedSlot != cancelSlot) return;
		e.setCancelled(true);

		// ✅ Potvrzení přidání
		if (clickedSlot == confirmSlot) {
			List<Map<String, Object>> newItems = new ArrayList<>();

			for (ItemStack item : inv.getContents()) {
				if (item == null || item.getType() == Material.AIR) continue;
				if (item.getType() == Material.EMERALD_BLOCK || item.getType() == Material.REDSTONE_BLOCK) continue;

				String base64 = ItemSerializer.itemToBase64(item);
				Map<String, Object> map = new HashMap<>();
				map.put("item", base64);
				map.put("chance", 50); // Default chance
				newItems.add(map);

				player.getInventory().addItem(item); // navrátit
			}

			YamlConfiguration data = NerKubLootChests.getInstance().getChestData();
			List<Map<?, ?>> existing = data.getMapList("chests." + chestName + ".items");
			existing.addAll(newItems);
			data.set("chests." + chestName + ".items", existing);
			NerKubLootChests.getInstance().saveChestData();

			player.sendMessage(MessageManager.get("messages.added_items",
					"count", String.valueOf(newItems.size()),
					"name", chestName));
			player.closeInventory();
			TempItemCache.endSession(player);

			Bukkit.getScheduler().runTaskLater(NerKubLootChests.getInstance(), () ->
					EditChestMenu.open(player, chestName), 2L);
		}

		// ❌ Zrušení přidávání
		if (clickedSlot == cancelSlot) {
			for (ItemStack item : inv.getContents()) {
				if (item == null || item.getType() == Material.AIR) continue;
				if (item.getType() == Material.EMERALD_BLOCK || item.getType() == Material.REDSTONE_BLOCK) continue;

				player.getInventory().addItem(item);
			}

			player.sendMessage(MessageManager.get("messages.canceled_add"));
			player.closeInventory();
			TempItemCache.endSession(player);

			Bukkit.getScheduler().runTaskLater(NerKubLootChests.getInstance(), () ->
					EditChestMenu.open(player, chestName), 2L);
		}
	}
}
