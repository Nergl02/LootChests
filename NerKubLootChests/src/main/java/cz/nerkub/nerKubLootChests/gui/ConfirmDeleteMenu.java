package cz.nerkub.nerKubLootChests.gui;

import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.ConfirmDeleteCache;
import cz.nerkub.nerKubLootChests.utils.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ConfirmDeleteMenu {

	public static void open(Player player, String chestName) {
		String title = MessageManager.get("gui.confirm_delete_title", "chest", chestName);
		Inventory inv = Bukkit.createInventory(null, 3 * 9, title);

		inv.setItem(11, GUIUtils.createItem(
				Material.EMERALD_BLOCK,
				MessageManager.get("gui.confirm_delete_yes"),
				MessageManager.getList("gui.confirm_delete_yes_lore", "chest", chestName)
		));

		inv.setItem(15, GUIUtils.createItem(
				Material.REDSTONE_BLOCK,
				MessageManager.get("gui.confirm_delete_no"),
				MessageManager.getList("gui.confirm_delete_no_lore")
		));

		ConfirmDeleteCache.set(player, chestName);
		player.openInventory(inv);
	}
}
