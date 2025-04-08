package cz.nerkub.nerKubLootChests.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class GUIUtils {

	public static ItemStack createItem(Material material, String displayName) {
		return createItem(material, displayName, List.of());
	}

	public static ItemStack createItem(Material material, String displayName, List<String> lore) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();

		if (meta != null) {
			meta.setDisplayName(displayName);
			if (lore != null) meta.setLore(lore);
			item.setItemMeta(meta);
		}

		return item;
	}

	public static ItemStack createItem(Material material, String displayName, String... lore) {
		return createItem(material, displayName, Arrays.asList(lore));
	}
}
