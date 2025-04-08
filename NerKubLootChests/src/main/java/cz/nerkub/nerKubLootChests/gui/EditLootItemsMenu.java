package cz.nerkub.nerKubLootChests.gui;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.GUIUtils;
import cz.nerkub.nerKubLootChests.utils.ItemSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditLootItemsMenu {

	public static void open(Player player, String chestName) {
		YamlConfiguration data = NerKubLootChests.getInstance().getChestData();
		List<Map<?, ?>> items = data.getMapList("chests." + chestName + ".items");

		String title = MessageManager.get("gui.loot_items_title", "chest", chestName);
		Inventory gui = Bukkit.createInventory(null, 6 * 9, title);

		int index = 0;
		for (Map<?, ?> entry : items) {
			if (index >= 45) break;

			String base64 = (String) entry.get("item");

			int chance = 100;
			Object chanceObj = entry.get("chance");
			if (chanceObj instanceof Number) {
				chance = ((Number) chanceObj).intValue();
			}

			String rarity = "COMMON";
			Object rarityObj = entry.get("rarity");
			if (rarityObj instanceof String) {
				rarity = ((String) rarityObj).toUpperCase();
			}

			ItemStack item = ItemSerializer.itemFromBase64(base64);
			ItemMeta meta = item.getItemMeta();
			if (meta != null) {
				List<String> lore = MessageManager.getList("gui.loot_item_lore",
						"chance", String.valueOf(chance),
						"rarity", rarity);
				meta.setLore(lore);
				item.setItemMeta(meta);
			}

			gui.setItem(index++, item);
		}

		gui.setItem(53, GUIUtils.createItem(Material.BARRIER, MessageManager.get("gui.back")));
		player.openInventory(gui);
	}

	private static String getRarityColor(String rarity) {
		return switch (rarity.toUpperCase()) {
			case "COMMON" -> "§7";
			case "UNCOMMON" -> "§a";
			case "RARE" -> "§9";
			case "EPIC" -> "§d";
			case "LEGENDARY" -> "§6";
			default -> "§f";
		};
	}
}
