package cz.nerkub.nerKubLootChests.utils;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ConfirmDeleteCache {
	private static final Map<Player, String> cache = new HashMap<>();

	public static void set(Player player, String chestName) {
		if (NerKubLootChests.getInstance().getConfig().getBoolean("debug")) {
			System.out.println("[ConfirmDeleteCache] SET " + player.getName() + " = " + chestName);
		}
		cache.put(player, chestName);
	}

	public static String get(Player player) {
		String value = cache.get(player);
		if (NerKubLootChests.getInstance().getConfig().getBoolean("debug")) {
			System.out.println("[ConfirmDeleteCache] GET " + player.getName() + " = " + value);
		}
		return value;
	}

	public static void remove(Player player) {
		if (NerKubLootChests.getInstance().getConfig().getBoolean("debug")) {
			System.out.println("[ConfirmDeleteCache] REMOVE " + player.getName());
		}
		cache.remove(player);
	}
}
