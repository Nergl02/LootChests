package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.gui.AddLootItemsMenu;
import cz.nerkub.nerKubLootChests.gui.BlockTypeSelectorMenu;
import cz.nerkub.nerKubLootChests.gui.ConfirmDeleteMenu;
import cz.nerkub.nerKubLootChests.gui.EditLootItemsMenu;
import cz.nerkub.nerKubLootChests.managers.ChestDataManager;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.ConfirmDeleteCache;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EditChestMenuListener implements Listener {

	@EventHandler
	public void onEditMenuClick(InventoryClickEvent event) {
		Inventory inv = event.getView().getTopInventory();
		ItemStack clicked = event.getCurrentItem();
		Player player = (Player) event.getWhoClicked();

		if (inv == null || clicked == null || clicked.getType() == Material.AIR) return;

		String actualTitle = ChatColor.stripColor(event.getView().getTitle());

		String expectedPrefix = ChatColor.stripColor(MessageManager.get("gui.edit_chest_title", "chest", "{chest}")).replace("{chest}", "").trim();
		if (!actualTitle.startsWith(expectedPrefix)) return;

		String chestName = actualTitle.substring(expectedPrefix.length()).trim().toLowerCase();

		event.setCancelled(true);

		switch (event.getSlot()) {
			case 10 -> {
				player.closeInventory();
				player.sendMessage(MessageManager.get("messages.chat_display_name"));
				ConfirmDeleteCache.set(player, chestName);
				ChestDataManager.awaitDisplayNameInput(player, chestName);
			}

			case 12 -> {
				player.closeInventory();
				player.sendMessage(MessageManager.get("messages.chat_refresh_time"));
				ConfirmDeleteCache.set(player, chestName);
				ChestDataManager.awaitRefreshTimeInput(player, chestName);
			}

			case 14 -> {
				player.closeInventory();
				player.sendMessage(MessageManager.get("messages.chat_items_per_refresh"));
				ConfirmDeleteCache.set(player, chestName);
				ChestDataManager.awaitItemsPerRefreshInput(player, chestName);
			}

			case 16 -> {
				player.closeInventory();
				ConfirmDeleteCache.set(player, chestName);
				EditLootItemsMenu.open(player, chestName);
			}

			case 28 -> {
				player.closeInventory();
				ConfirmDeleteCache.set(player, chestName);
				BlockTypeSelectorMenu.open(player, chestName);
			}


			case 30 -> {
				player.closeInventory();
				ConfirmDeleteCache.set(player, chestName);
				AddLootItemsMenu.open(player, chestName);
			}

			case 32 -> {
				player.closeInventory();
				ConfirmDeleteCache.set(player, chestName);
				ConfirmDeleteMenu.open(player, chestName);
			}
		}

	}
}
