
package cz.nerkub.nerKubLootChests.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class YamlUpdater {

	public static void update(File file, JavaPlugin plugin) {
		try {
			// Load existing config
			YamlConfiguration current = YamlConfiguration.loadConfiguration(file);

			// Load default config from inside the jar
			InputStream defConfigStream = plugin.getResource(file.getName());
			if (defConfigStream == null) return;
			YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));

			// Merge missing keys
			for (String key : defaults.getKeys(true)) {
				if (!current.contains(key)) {
					current.set(key, defaults.get(key));
				}
			}

			// Save updated config
			current.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
