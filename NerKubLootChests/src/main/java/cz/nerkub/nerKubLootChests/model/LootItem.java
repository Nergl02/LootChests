package cz.nerkub.nerKubLootChests.model;

import org.bukkit.inventory.ItemStack;

public class LootItem {
	private final ItemStack item;
	private final int chance;
	private final String rarity;

	public LootItem(ItemStack item, int chance, String rarity) {
		this.item = item;
		this.chance = chance;
		this.rarity = rarity;
	}

	public ItemStack getItem() {
		return item;
	}

	public int getChance() {
		return chance;
	}

	public String getRarity() {
		return rarity != null ? rarity.toUpperCase() : "COMMON";
	}
}
