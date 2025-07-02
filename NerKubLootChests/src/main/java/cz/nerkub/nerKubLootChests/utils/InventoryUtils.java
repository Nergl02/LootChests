package cz.nerkub.nerKubLootChests.utils;

import cz.nerkub.nerKubLootChests.managers.MessageManager;
import org.bukkit.ChatColor;

public class InventoryUtils {

	public static String extractChestNameFromTitle(String title) {
		String raw = ChatColor.stripColor(title);

		// Odeber prefix podle lokalizace
		String prefix = ChatColor.stripColor(MessageManager.get("gui.block_type_selector_title", "chest", "{chest}")).replace("{chest}", "").trim();

		if (!raw.startsWith(prefix)) return null;

		return raw.substring(prefix.length()).trim().toLowerCase();
	}
}
