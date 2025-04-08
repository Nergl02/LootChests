package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.commands.LootChestCommand;
import cz.nerkub.nerKubLootChests.gui.EditChestMenu;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.ConfirmDeleteCache;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ConfirmDeleteMenuListener implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player player)) return;

		ItemStack clicked = e.getCurrentItem();
		if (clicked == null || clicked.getType() == Material.AIR) return;

		String chestName = ConfirmDeleteCache.get(player);
		if (chestName == null) {
			player.sendMessage(MessageManager.get("messages.delete_cache_missing"));
			player.closeInventory();
			return;
		}

		String expectedTitle = ChatColor.stripColor(MessageManager.get("gui.confirm_delete_title", "chest", chestName));
		String actualTitle = ChatColor.stripColor(e.getView().getTitle());
		if (!actualTitle.equalsIgnoreCase(expectedTitle)) return;

		e.setCancelled(true);

		switch (clicked.getType()) {
			case EMERALD_BLOCK -> {
				LootChestCommand.deleteChest(player, chestName);
				player.closeInventory();
			}
			case REDSTONE_BLOCK -> {
				player.sendMessage(MessageManager.get("messages.delete_cancelled"));
				EditChestMenu.open(player, chestName);
			}
		}

		ConfirmDeleteCache.remove(player);
	}
}
