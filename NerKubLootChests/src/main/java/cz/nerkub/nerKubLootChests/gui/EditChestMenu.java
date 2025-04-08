package cz.nerkub.nerKubLootChests.gui;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class EditChestMenu implements Listener {

	public static void open(Player player, String chestName) {
		String title = MessageManager.get("gui.edit_chest_title", "chest", chestName);
		Inventory inv = Bukkit.createInventory(null, 6 * 9, title);

		YamlConfiguration data = NerKubLootChests.getInstance().getChestData();
		String displayName = data.getString("chests." + chestName + ".displayName", "§fChest");
		int refreshTime = data.getInt("chests." + chestName + ".refreshTime", 300);
		int itemsPer = data.getInt("chests." + chestName + ".itemsPerRefresh", 3);

		// 🏷 Display Name
		inv.setItem(10, GUIUtils.createItem(
				Material.NAME_TAG,
				MessageManager.get("gui.display_name_button"),
				MessageManager.getList("gui.display_name_lore", "value", displayName)
		));

		// ⏱ Refresh Time
		inv.setItem(12, GUIUtils.createItem(
				Material.CLOCK,
				MessageManager.get("gui.refresh_time_button"),
				MessageManager.getList("gui.refresh_time_lore", "value", String.valueOf(refreshTime))
		));

		// 📦 Items Per Refresh
		inv.setItem(14, GUIUtils.createItem(
				Material.HOPPER,
				MessageManager.get("gui.items_per_refresh_button"),
				MessageManager.getList("gui.items_per_refresh_lore", "value", String.valueOf(itemsPer))
		));

		// 🎁 Edit Loot
		inv.setItem(16, GUIUtils.createItem(Material.CHEST, MessageManager.get("gui.edit_loot_items_button")));

		// ➕ Add items
		inv.setItem(30, GUIUtils.createItem(Material.EMERALD_BLOCK, MessageManager.get("gui.add_items_button")));

		// ❌ Delete
		inv.setItem(32, GUIUtils.createItem(Material.BARRIER, MessageManager.get("gui.delete_button")));

		player.openInventory(inv);
	}
}
