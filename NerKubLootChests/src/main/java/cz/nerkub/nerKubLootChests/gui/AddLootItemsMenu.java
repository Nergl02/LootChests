package cz.nerkub.nerKubLootChests.gui;

import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.GUIUtils;
import cz.nerkub.nerKubLootChests.utils.TempItemCache;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class AddLootItemsMenu {

	public static void open(Player player, String chestName) {
		String title = MessageManager.get("gui.add_items_title", "chest", chestName);
		Inventory inv = Bukkit.createInventory(null, 6 * 9, title);

		// ✅ Confirm
		inv.setItem(6 * 9 - 6, GUIUtils.createItem(
				Material.EMERALD_BLOCK,
				MessageManager.get("gui.confirm_add_items")
		));

		// ❌ Cancel
		inv.setItem(6 * 9 - 4, GUIUtils.createItem(
				Material.REDSTONE_BLOCK,
				MessageManager.get("gui.cancel_add_items")
		));

		TempItemCache.startSession(player, chestName);
		player.openInventory(inv);
	}
}
