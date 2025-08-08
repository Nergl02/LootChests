package cz.nerkub.nerKubLootChests.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TempItemCache {

	private static final Map<Player, String> chestTargets = new HashMap<>();
	private static final Map<Player, EditingItemInfo> editingItems = new HashMap<>();

	// 🔐 Session control
	public static void startSession(Player player, String chestName) {
		chestTargets.put(player, chestName);
		editingItems.remove(player); // <- pokud začíná nová session
	}

	public static String getChestName(Player player) {
		return chestTargets.get(player);
	}

	public static boolean isInSession(Player player) {
		return chestTargets.containsKey(player);
	}

	public static void endSession(Player player) {
		chestTargets.remove(player);
		editingItems.remove(player);
	}

	// 📦 Editing item info (for chance editing)
	public static void setEditingItem(Player player, String chestName, int index, int page, ItemStack item, int chance, String rarity) {
		editingItems.put(player, new EditingItemInfo(chestName, index, page, item, chance, rarity));
	}

	public static EditingItemInfo getEditingItem(Player player) {
		return editingItems.get(player);
	}

	public static boolean isEditingItem(Player player) {
		return editingItems.containsKey(player);
	}

	public static class EditingItemInfo {
		public final String chestName;
		public final int index;
		public final int page; // ✅ Přidáno
		public final ItemStack item;
		public int chance;
		public String rarity;

		public EditingItemInfo(String chestName, int index, int page, ItemStack item, int chance, String rarity) {
			this.chestName = chestName;
			this.index = index;
			this.page = page;
			this.item = item;
			this.chance = chance;
			this.rarity = rarity;
		}
	}
}
