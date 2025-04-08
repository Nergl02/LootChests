package cz.nerkub.nerKubLootChests.gui;

import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.GUIUtils;
import cz.nerkub.nerKubLootChests.utils.TempItemCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ChanceEditorMenu {

	public static void open(Player player) {
		String title = MessageManager.get("gui.chance_editor_title");
		Inventory inv = Bukkit.createInventory(null, 6 * 9, title);

		var editing = TempItemCache.getEditingItem(player);
		if (editing == null) return;

		ItemStack item = editing.item.clone();
		inv.setItem(4, item);

		String rarityColor = getRarityColor(editing.rarity);

		inv.setItem(13, GUIUtils.createItem(
				Material.NAME_TAG,
				ChatColor.GOLD + "Current Values",
				List.of(
						ChatColor.YELLOW + "Chance: " + ChatColor.GREEN + editing.chance + "%",
						ChatColor.YELLOW + "Rarity: " + getRarityColor(editing.rarity) + editing.rarity
				)
		));


		inv.setItem(29, GUIUtils.createItem(Material.LIME_STAINED_GLASS_PANE, MessageManager.get("gui.chance_plus_10")));
		inv.setItem(30, GUIUtils.createItem(Material.GREEN_STAINED_GLASS_PANE, MessageManager.get("gui.chance_plus_1")));
		inv.setItem(32, GUIUtils.createItem(Material.RED_STAINED_GLASS_PANE, MessageManager.get("gui.chance_minus_1")));
		inv.setItem(33, GUIUtils.createItem(Material.ORANGE_STAINED_GLASS_PANE, MessageManager.get("gui.chance_minus_10")));

		inv.setItem(20, createRarityItem("COMMON", Material.COAL_BLOCK, editing.rarity));
		inv.setItem(21, createRarityItem("UNCOMMON", Material.IRON_BLOCK, editing.rarity));
		inv.setItem(22, createRarityItem("RARE", Material.GOLD_BLOCK, editing.rarity));
		inv.setItem(23, createRarityItem("EPIC", Material.LAPIS_BLOCK, editing.rarity));
		inv.setItem(24, createRarityItem("LEGENDARY", Material.DIAMOND_BLOCK, editing.rarity));

		inv.setItem(48, GUIUtils.createItem(Material.EMERALD_BLOCK, MessageManager.get("gui.save")));
		inv.setItem(50, GUIUtils.createItem(Material.BARRIER, MessageManager.get("gui.cancel")));

		player.openInventory(inv);
	}

	private static ItemStack createRarityItem(String rarity, Material material, String selected) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		meta.setDisplayName(MessageManager.get("gui.rarity_" + rarity.toLowerCase()));
		if (rarity.equalsIgnoreCase(selected)) {
			meta.addEnchant(Enchantment.UNBREAKING, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		item.setItemMeta(meta);
		return item;
	}

	public static String getRarityColor(String rarity) {
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

