package cz.nerkub.nerKubLootChests.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemBuilder {
	private final ItemStack item;

	public ItemBuilder(Material material) {
		this.item = new ItemStack(material);
	}

	public ItemBuilder setName(String name) {
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(name);
			item.setItemMeta(meta);
		}
		return this;
	}

	public ItemBuilder setLore(List<String> lore) {
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setLore(lore);
			item.setItemMeta(meta);
		}
		return this;
	}

	public ItemBuilder glow() {
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.addEnchant(Enchantment.UNBREAKING, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			item.setItemMeta(meta);
		}
		return this;
	}

	public ItemStack build() {
		return item;
	}
}
