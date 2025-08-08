package cz.nerkub.nerKubLootChests.gui;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.GUIUtils;
import cz.nerkub.nerKubLootChests.utils.ItemSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EditLootItemsMenu {

	public static void open(Player player, String chestName, int page) {
		YamlConfiguration data = NerKubLootChests.getInstance().getChestData();
		List<Map<?, ?>> items = data.getMapList("chests." + chestName + ".items");
		if (items == null) items = new ArrayList<>();

		int itemsPerPage = 45;
		int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
		if (page < 0) page = 0;
		if (page >= totalPages) page = totalPages - 1;

		String baseTitle = MessageManager.get("gui.loot_items_title", "chest", chestName);
		String title = ChatColor.DARK_GRAY + baseTitle + " - Page " + (page + 1);
		Inventory gui = Bukkit.createInventory(null, 6 * 9, title);

		int startIndex = page * itemsPerPage;
		int endIndex = Math.min(startIndex + itemsPerPage, items.size());

		for (int i = startIndex; i < endIndex; i++) {
			Map<?, ?> entry = items.get(i);
			String base64 = (String) entry.get("item");

			int chance = entry.get("chance") instanceof Number ? ((Number) entry.get("chance")).intValue() : 100;
			String rarity = entry.get("rarity") instanceof String ? ((String) entry.get("rarity")).toUpperCase() : "COMMON";

			ItemStack item = ItemSerializer.itemFromBase64(base64);
			ItemMeta meta = item.getItemMeta();
			if (meta != null) {
				List<String> lore = MessageManager.getList("gui.loot_item_lore",
						"chance", String.valueOf(chance),
						"rarity", rarity);
				meta.setLore(lore);
				item.setItemMeta(meta);
			}
			gui.setItem(i - startIndex, item); // GUI slot
		}

		// Navigační tlačítka
		// Navigační tlačítka
		if (page > 0)
			gui.setItem(45, GUIUtils.createItem(Material.ARROW, MessageManager.get("gui.previous_page_button")));

		if (page < totalPages - 1)
			gui.setItem(53, GUIUtils.createItem(Material.ARROW, MessageManager.get("gui.next_page_button")));

		gui.setItem(49, GUIUtils.createItem(Material.BARRIER, MessageManager.get("gui.back")));
		player.openInventory(gui);
	}
}
