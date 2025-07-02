package cz.nerkub.nerKubLootChests.gui;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.managers.ChestDataManager;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.utils.ConfirmDeleteCache;
import cz.nerkub.nerKubLootChests.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class BlockTypeSelectorMenu {

	private static final List<Material> validTypes = Arrays.asList(
			Material.CHEST,
			Material.BARREL,
			Material.SHULKER_BOX,
			Material.RED_SHULKER_BOX,
			Material.BLUE_SHULKER_BOX,
			Material.CYAN_SHULKER_BOX,
			Material.BLACK_SHULKER_BOX,
			Material.GREEN_SHULKER_BOX,
			Material.LIGHT_BLUE_SHULKER_BOX,
			Material.MAGENTA_SHULKER_BOX,
			Material.GRAY_SHULKER_BOX,
			Material.LIGHT_GRAY_SHULKER_BOX,
			Material.ORANGE_SHULKER_BOX,
			Material.PINK_SHULKER_BOX,
			Material.PURPLE_SHULKER_BOX,
			Material.YELLOW_SHULKER_BOX,
			Material.LIME_SHULKER_BOX,
			Material.BROWN_SHULKER_BOX,
			Material.WHITE_SHULKER_BOX
	);

	public static void open(Player player, String chestName) {
		String title = MessageManager.get("gui.block_type_selector_title", "chest", chestName);
		Inventory gui = Bukkit.createInventory(null, 3 * 9, title);

		YamlConfiguration data = NerKubLootChests.getInstance().getChestData();
		Material currentType = Material.CHEST; // default

		String typeStr = data.getString("chests." + chestName + ".blockType");
		if (typeStr != null) {
			try {
				currentType = Material.valueOf(typeStr.toUpperCase());
			} catch (IllegalArgumentException ignored) {}
		}

		int index = 0;
		for (Material type : validTypes) {
			if (index >= gui.getSize()) break;

			ItemBuilder builder = new ItemBuilder(type)
					.setName("§f" + type.name().replace("_", " ").toLowerCase());

			List<String> lore = MessageManager.getList("gui.block_type_selector_lore");
			builder.setLore(lore);

			if (type == currentType) builder.glow();

			gui.setItem(index++, builder.build());
		}

		ConfirmDeleteCache.set(player, chestName);
		player.openInventory(gui);
	}

}
