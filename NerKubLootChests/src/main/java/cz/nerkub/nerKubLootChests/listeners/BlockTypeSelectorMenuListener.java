package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.gui.EditChestMenu;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.ChestUtils;
import cz.nerkub.nerKubLootChests.utils.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class BlockTypeSelectorMenuListener implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player player)) return;
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

		Inventory inv = e.getInventory();
		String title = e.getView().getTitle();
		if (!title.contains(MessageManager.get("gui.blocktype_title_prefix"))) return;

		e.setCancelled(true);

		String chestName = InventoryUtils.extractChestNameFromTitle(title);
		if (chestName == null) return;

		// Zpět
		if (e.getSlot() == 22) {
			EditChestMenu.open(player, chestName);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			return;
		}

		Material selected = e.getCurrentItem().getType();
		YamlConfiguration config = NerKubLootChests.getInstance().getChestData();
		config.set("chests." + chestName + ".blockType", selected.name());
		NerKubLootChests.getInstance().saveChestData();

		player.sendMessage(MessageManager.get("messages.blocktype_selected", "material", selected.name()));
		ChestUtils.updateAllBlocksToCorrectType(chestName);
		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
		EditChestMenu.open(player, chestName);
	}
}
