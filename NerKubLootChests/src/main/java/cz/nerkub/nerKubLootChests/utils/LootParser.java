package cz.nerkub.nerKubLootChests.utils;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.model.LootItem;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LootParser {

	public static List<LootItem> getLootItems(String chestName) {
		YamlConfiguration data = NerKubLootChests.getInstance().getChestData();
		List<Map<?, ?>> rawList = data.getMapList("chests." + chestName + ".items");

		List<LootItem> result = new ArrayList<>();

		for (Map<?, ?> raw : rawList) {
			try {
				String base64 = (String) raw.get("item");

				int chance = 100;
				Object rawChance = raw.get("chance");
				if (rawChance instanceof Number) {
					chance = ((Number) rawChance).intValue();
				}

				String rarity = "COMMON";
				Object rawRarity = raw.get("rarity");
				if (rawRarity instanceof String) {
					rarity = (String) rawRarity;
				}

				ItemStack item = ItemSerializer.itemFromBase64(base64);
				result.add(new LootItem(item, chance, rarity));
			} catch (Exception e) {
				System.out.println("Error while loading item in chest '" + chestName + "': " + e.getMessage());
			}

		}

		return result;
	}
}
