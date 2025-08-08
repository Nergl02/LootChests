package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.gui.ChanceEditorMenu;
import cz.nerkub.nerKubLootChests.gui.EditChestMenu;
import cz.nerkub.nerKubLootChests.gui.EditLootItemsMenu;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditLootItemsListener implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player player)) return;

		String rawTitle = ChatColor.stripColor(e.getView().getTitle());
		String chestName = extractChestName(rawTitle);
		int currentPage = extractPageNumber(rawTitle);

		if (chestName == null || currentPage == -1) return;

		e.setCancelled(true);
		ItemStack clicked = e.getCurrentItem();
		if (clicked == null || clicked.getType().isAir()) return;

		ItemMeta meta = clicked.getItemMeta();
		String itemName = meta != null && meta.hasDisplayName()
				? ChatColor.stripColor(meta.getDisplayName())
				: "";

		// ✨ Překladové názvy tlačítek
		String previousPageName = ChatColor.stripColor(MessageManager.get("gui.previous_page_button"));
		String nextPageName = ChatColor.stripColor(MessageManager.get("gui.next_page_button"));
		String backButtonName = ChatColor.stripColor(MessageManager.get("gui.back"));

		// 🧭 Detekce navigačních tlačítek podle názvu
		if (itemName.equalsIgnoreCase(previousPageName)) {
			EditLootItemsMenu.open(player, chestName, currentPage - 1);
			return;
		}
		if (itemName.equalsIgnoreCase(nextPageName)) {
			EditLootItemsMenu.open(player, chestName, currentPage + 1);
			return;
		}
		if (itemName.equalsIgnoreCase(backButtonName)) {
			EditChestMenu.open(player, chestName);
			return;
		}

		// Klik mimo hlavní oblast s itemy
		if (e.getSlot() < 0 || e.getSlot() > 44) return;

		YamlConfiguration data = NerKubLootChests.getInstance().getChestData();
		List<Map<?, ?>> items = data.getMapList("chests." + chestName + ".items");
		if (items == null) items = new ArrayList<>();

		ItemStack compareItem = clicked.clone();
		if (compareItem.hasItemMeta()) {
			ItemMeta copyMeta = compareItem.getItemMeta();
			copyMeta.setLore(null);
			compareItem.setItemMeta(copyMeta);
		}
		String clickedBase64 = ItemSerializer.itemToBase64(compareItem);

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

			EditLootItemsMenu.open(player, chestName, currentPage);
			return;
		}

		// ✏️ Normální kliknutí → úprava šance a rarity
		int chance = 50;
		String rarity = "COMMON";

		for (Map<?, ?> map : items) {
			if (clickedBase64.equals(map.get("item"))) {
				Object chanceObj = map.get("chance");
				if (chanceObj instanceof Number) chance = ((Number) chanceObj).intValue();

				Object rarityObj = map.get("rarity");
				if (rarityObj instanceof String) rarity = ((String) rarityObj).toUpperCase();
				break;
			}
		}

		int globalIndex = (currentPage * 45) + e.getSlot();
		TempItemCache.startSession(player, chestName);
		TempItemCache.setEditingItem(player, chestName, globalIndex, currentPage, clicked.clone(), chance, rarity);
		ChanceEditorMenu.open(player);
	}


	// 🎯 Pomocné metody pro rozpoznání chest name a page z lokalizovaného titulu
	private String extractChestName(String title) {
		String translated = ChatColor.stripColor(MessageManager.get("gui.loot_items_title", "chest", ""));
		if (!title.contains(translated)) return null;

		int colonIndex = translated.indexOf(":");
		if (colonIndex == -1) return null;

		int start = title.indexOf(":") + 1;
		int end = title.lastIndexOf(" - Page");
		if (start == -1 || end == -1 || end <= start) return null;

		return title.substring(start, end).trim().toLowerCase();
	}

	private int extractPageNumber(String title) {
		Pattern pagePattern = Pattern.compile(" - Page (\\d+)");
		Matcher matcher = pagePattern.matcher(title);
		if (matcher.find()) {
			try {
				return Integer.parseInt(matcher.group(1)) - 1;
			} catch (NumberFormatException ignored) {
			}
		}
		return -1;
	}
}
