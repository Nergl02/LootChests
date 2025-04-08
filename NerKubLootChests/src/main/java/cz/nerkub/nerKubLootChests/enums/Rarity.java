package cz.nerkub.nerKubLootChests.enums;

public enum Rarity {
	COMMON,
	UNCOMMON,
	RARE,
	EPIC,
	LEGENDARY;

	public static Rarity fromString(String input) {
		try {
			return Rarity.valueOf(input.toUpperCase());
		} catch (IllegalArgumentException e) {
			return COMMON; // Default fallback
		}
	}
}
