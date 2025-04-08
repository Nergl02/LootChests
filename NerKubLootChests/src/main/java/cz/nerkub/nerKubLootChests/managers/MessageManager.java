package cz.nerkub.nerKubLootChests.managers;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageManager {

	private static YamlConfiguration messages;
	private static File file;


	public static void init(NerKubLootChests plugin) {
		file = new File(plugin.getDataFolder(), "messages.yml");

		if (!file.exists()) {
			plugin.saveResource("messages.yml", false);
		}
		messages = YamlConfiguration.loadConfiguration(file);
	}

	public static String get(String path, String... replacements) {
		String msg = messages.getString(path);

		if (msg == null) return ChatColor.RED + "Missing message: " + path;

		// Prefix
		String prefix = messages.getString("prefix", "");
		msg = msg.replace("{prefix}", prefix);

		// Replace placeholders
		for (int i = 0; i < replacements.length - 1; i += 2) {
			String key = replacements[i];
			String value = replacements[i + 1];

			if (key == null || value == null) continue; // ⚠️ ochrana proti null

			msg = msg.replace("{" + key + "}", value);
		}

		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public static List<String> getList(String path, Object... placeholders) {
		List<String> rawList = messages.getStringList(path);

		if (rawList == null || rawList.isEmpty()) {
			rawList = List.of(ChatColor.RED + "Missing message list: " + path);
		}

		List<String> result = new ArrayList<>();

		for (String line : rawList) {
			for (int i = 0; i < placeholders.length - 1; i += 2) {
				String key = String.valueOf(placeholders[i]);
				String value = String.valueOf(placeholders[i + 1]);

				if (key == null || value == null) continue;

				line = line.replace("{" + key + "}", value);
			}
			result.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		return result;
	}


	public static String format(String path, Map<String, String> placeholders) {
		String msg = get(path);
		for (Map.Entry<String, String> entry : placeholders.entrySet()) {
			msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
		}
		return msg;
	}
}

