package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.gui.ChanceEditorMenu;
import cz.nerkub.nerKubLootChests.gui.EditChestMenu;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.ItemSerializer;
import cz.nerkub.nerKubLootChests.utils.TempItemCache;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EditLootItemsListener implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player player)) return;

		String actualTitle = ChatColor.stripColor(e.getView().getTitle());
		String expectedPrefix = ChatColor.stripColor(MessageManager.get("gui.loot_items_title", "chest", "{chest}")).replace("{chest}", "").trim();

		if (!actualTitle.startsWith(expectedPrefix)) return;

		String chestName = actualTitle.substring(expectedPrefix.length()).trim().toLowerCase();

		e.setCancelled(true);
		ItemStack clicked = e.getCurrentItem();
		if (clicked == null || clicked.getType().isAir()) return;

		if (e.getSlot() == 53) {
			EditChestMenu.open(player, chestName);
			return;
		}

		ItemStack compareItem = clicked.clone();
		if (compareItem.hasItemMeta()) {
			ItemMeta meta = compareItem.getItemMeta();
			meta.setLore(null);
			compareItem.setItemMeta(meta);
		}

		String clickedBase64 = ItemSerializer.itemToBase64(compareItem);
		YamlConfiguration data = NerKubLootChests.getInstance().getChestData();
		List<Map<?, ?>> items = data.getMapList("chests." + chestName + ".items");

		// 🧹 SHIFT + pravý klik = smazání itemu
		if (e.isShiftClick() && e.isRightClick()) {
			List<Map<?, ?>> updated = new ArrayList<>();
			boolean removed = false;

			for (Map<?, ?> map : items) {
				Object raw = map.get("item");
				if (!(raw instanceof String)) continue;

				String mapBase64 = (String) raw;
				if (!removed && mapBase64.equals(clickedBase64)) {
					removed = true;
					continue;
				}
				updated.add(map);
			}

			if (removed) {
				data.set("chests." + chestName + ".items", updated);
				NerKubLootChests.getInstance().saveChestData();
				player.sendMessage(MessageManager.get("messages.removed_item"));
			} else {
				player.sendMessage(MessageManager.get("messages.item_not_found"));
			}

			EditChestMenu.open(player, chestName);
			return;
		}

		// ✏️ Normální kliknutí → úprava šance a rarity
		int chance = 50;
		String rarity = "COMMON";

		for (Map<?, ?> map : items) {
			if (clickedBase64.equals(map.get("item"))) {
				Object chanceObj = map.get("chance");
				if (chanceObj instanceof Number) {
					chance = ((Number) chanceObj).intValue();
				}

				Object rarityObj = map.get("rarity");
				if (rarityObj instanceof String) {
					rarity = ((String) rarityObj).toUpperCase();
				}
				break;
			}
		}

		TempItemCache.startSession(player, chestName);
		TempItemCache.setEditingItem(player, chestName, e.getSlot(), clicked.clone(), chance, rarity);
		ChanceEditorMenu.open(player);
	}
}
