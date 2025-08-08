package cz.nerkub.nerKubLootChests.utils;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public class ChestUtils {

	public static String getChestNameAtLocation(Location loc) {
		var data = NerKubLootChests.getInstance().getChestData();
		if (!data.isConfigurationSection("chests")) return null;

		for (String chestName : data.getConfigurationSection("chests").getKeys(false)) {
			ConfigurationSection chest = data.getConfigurationSection("chests." + chestName);
			if (chest == null) continue;

			for (var locMap : chest.getMapList("locations")) {
				Location storedLoc = Location.deserialize((Map<String, Object>) locMap);
				if (storedLoc.getWorld().equals(loc.getWorld()) && storedLoc.getBlock().getLocation().equals(loc.getBlock().getLocation())) {
					return chestName;
				}

			}
		}

		return null;
	}

	public static void updateAllBlocksToCorrectType(String chestName) {
		YamlConfiguration data = NerKubLootChests.getInstance().getChestData();
		String matName = data.getString("chests." + chestName + ".blockType", "CHEST");
		Material desiredMaterial = Material.matchMaterial(matName);

		if (desiredMaterial == null) {
			Bukkit.getLogger().warning("❌ Invalid material in blockType: " + matName);
			return;
		}

		List<Map<?, ?>> locMaps = data.getMapList("chests." + chestName + ".locations");
		NamespacedKey key = new NamespacedKey(NerKubLootChests.getInstance(), "lootchest");


		for (Map<?, ?> raw : locMaps) {
			Location loc = Location.deserialize((Map<String, Object>) raw);

			if (!loc.getChunk().isLoaded()) continue; // Skip unloaded chunks
			Block block = loc.getBlock();

			if (block.getType() == desiredMaterial) continue; // Already correct type

			// Replace block type
			block.setType(desiredMaterial);

			// Restore NBT data
			if (block.getState() instanceof TileState tileState) {
				tileState.getPersistentDataContainer().set(key, PersistentDataType.STRING, chestName);
				tileState.update();
			}
		}
	}

	// ChestUtils.java
	public static void applyGuiTitleToAllBlocks(String chestName) {
		YamlConfiguration data = NerKubLootChests.getInstance().getChestData();
		String guiTitle = data.getString("chests." + chestName + ".guiTitle",
				data.getString("chests." + chestName + ".displayName", chestName));
		String colored = ChatColor.translateAlternateColorCodes('&', guiTitle);

		List<Map<?, ?>> locMaps = data.getMapList("chests." + chestName + ".locations");
		if (locMaps == null) return;

		Bukkit.getScheduler().runTask(NerKubLootChests.getInstance(), () -> {
			for (Map<?, ?> raw : locMaps) {
				Location loc = Location.deserialize((Map<String, Object>) raw);
				if (loc.getWorld() == null) continue;
				Block block = loc.getBlock();
				if (!(block.getState() instanceof org.bukkit.block.Container container)) continue;
				container.setCustomName(colored); // <- důležité
				container.update();
			}
		});
	}


}
