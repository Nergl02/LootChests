package cz.nerkub.nerKubLootChests.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.StringReader;
import java.util.Base64;

public class ItemSerializer {

	public static String itemToBase64(ItemStack item) {
		YamlConfiguration config = new YamlConfiguration();
		config.set("item", item);
		return Base64.getEncoder().encodeToString(config.saveToString().getBytes());
	}

	public static ItemStack itemFromBase64(String base64) {
		try {
			String yaml = new String(Base64.getDecoder().decode(base64));
			YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader(yaml));
			return config.getItemStack("item");
		} catch (Exception e) {
			throw new RuntimeException("Cannot deserialize item!", e);
		}
	}
}
