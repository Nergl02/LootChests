package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.gui.ChanceEditorMenu;
import cz.nerkub.nerKubLootChests.gui.EditLootItemsMenu;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.TempItemCache;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Map;

public class ChanceEditorListener implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player player)) return;

		String title = ChatColor.stripColor(e.getView().getTitle());
		if (!title.equalsIgnoreCase(MessageManager.get("gui.chance_editor_title"))) return;
		e.setCancelled(true);

		var editing = TempItemCache.getEditingItem(player);
		if (editing == null) return;

		switch (e.getSlot()) {
			// 🌈 Nové rarity sloty
			case 20 -> editing.rarity = "COMMON";
			case 21 -> editing.rarity = "UNCOMMON";
			case 22 -> editing.rarity = "RARE";
			case 23 -> editing.rarity = "EPIC";
			case 24 -> editing.rarity = "LEGENDARY";

			// 📈 Zvýšení šance
			case 29 -> editing.chance = Math.min(100, editing.chance + 10);
			case 30 -> editing.chance = Math.min(100, editing.chance + 1);

			// 📉 Snížení šance
			case 32 -> editing.chance = Math.max(1, editing.chance - 1);
			case 33 -> editing.chance = Math.max(1, editing.chance - 10);

			// ❌ Cancel
			case 50 -> {
				player.sendMessage(MessageManager.get("messages.edit_canceled"));
				TempItemCache.endSession(player);
				EditLootItemsMenu.open(player, editing.chestName, editing.page);
				return;
			}

// ✅ Save
			case 48 -> {
				YamlConfiguration data = NerKubLootChests.getInstance().getChestData();
				List<Map<String, Object>> items = (List<Map<String, Object>>) (List<?>) data.getMapList("chests." + editing.chestName + ".items");

				if (editing.index >= 0 && editing.index < items.size()) {
					Map<String, Object> map = items.get(editing.index);
					map.put("chance", editing.chance);
					map.put("rarity", editing.rarity);
					data.set("chests." + editing.chestName + ".items", items);
					NerKubLootChests.getInstance().saveChestData();

					player.sendMessage(MessageManager.get("messages.saved_changes",
							"chance", String.valueOf(editing.chance),
							"rarity", editing.rarity,
							"rarity_color", ChanceEditorMenu.getRarityColor(editing.rarity)));
				} else {
					player.sendMessage(MessageManager.get("messages.invalid_index"));
				}

				TempItemCache.endSession(player);
				EditLootItemsMenu.open(player, editing.chestName, editing.page);
				return;
			}
		}

		// 🔁 Refresh GUI
		ChanceEditorMenu.open(player);
	}

	private String getRarityColor(String rarity) {
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
