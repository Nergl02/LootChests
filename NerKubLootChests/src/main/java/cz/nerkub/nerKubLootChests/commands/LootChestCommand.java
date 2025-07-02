package cz.nerkub.nerKubLootChests.commands;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import cz.nerkub.nerKubLootChests.SupportedContainers;
import cz.nerkub.nerKubLootChests.gui.EditChestMenu;
import cz.nerkub.nerKubLootChests.managers.HologramManager;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootChestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(MessageManager.get("messages.not_player"));
			return true;
		}

		// 🛡️ Permission check
		if (!player.hasPermission("lootchest.admin")) {
			player.sendMessage(MessageManager.get("messages.no_permission"));
			return true;
		}

		if (args.length < 1 ) {
			sender.sendMessage(MessageManager.get("messages.usage"));
			return true;
		}

		String sub = args[0].toLowerCase();
		switch (sub) {
			case "create":
				handleCreate(player, args);
				break;
			case "add":
				handleAdd(player, args);
				break;
			case "loottime":
				handleLootTime(player, args);
				break;
			case "delete":
				handleDelete(player, args);
				break;
			case "get":
				handleGet(player, args);
				break;
			case "edit":
				handleEdit(player, args);
				break;
			case "reload":
				handleReload(player);
				break;
			default:
				sender.sendMessage(MessageManager.get("messages.subcommand_invalid"));
		}

		return true;
	}

	private void handleCreate(Player player, String[] args) {
		if (args.length < 2) {
			player.sendMessage(MessageManager.get("messages.no_permission"));
			return;
		}

		String chestName = args[1].toLowerCase();
		File chestFile = new File(NerKubLootChests.getInstance().getDataFolder(), "chests.yml");
		YamlConfiguration chestData = NerKubLootChests.getInstance().getChestData(); // <- získáš instanci
		NerKubLootChests.getInstance().saveChestData(); // uloží na disk


		if (chestData.contains("chests." + chestName)) {
			player.sendMessage(MessageManager.get("messages.already_exists"));
			return;
		}

		String displayName = chestName.substring(0, 1).toUpperCase() + chestName.substring(1); // auto-capitalize

		chestData.set("chests." + chestName + ".displayName", displayName);
		chestData.set("chests." + chestName + ".location", null);
		chestData.set("chests." + chestName + ".refreshTime", 300);
		chestData.set("chests." + chestName + ".itemsPerRefresh", 3);
		chestData.set("chests." + chestName + ".items", new java.util.ArrayList<>());

		try {
			chestData.save(chestFile);
		} catch (IOException e) {
			player.sendMessage(MessageManager.get("messages.create_error"));
			e.printStackTrace();
			return;
		}

		// Vytvoření chest itemu s NBT tagem
		ItemStack chest = new ItemStack(Material.CHEST);
		ItemMeta meta = chest.getItemMeta();
		assert meta != null;
		meta.setDisplayName(ChatColor.GOLD + "LootChest: " + chestName);

		// NBT tag přes PersistentDataContainer
		NamespacedKey key = new NamespacedKey(NerKubLootChests.getInstance(), "lootchest");
		meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, chestName);
		chest.setItemMeta(meta);

		player.getInventory().addItem(chest);
		player.sendMessage(MessageManager.get("messages.created", "name", chestName));

	}

	private void handleDelete(Player player, String[] args) {
		if (args.length < 2) {
			player.sendMessage(MessageManager.get("messages.usage_delete"));
			return;
		}

		String chestName = args[1].toLowerCase();
		var chestData = NerKubLootChests.getInstance().getChestData();

		if (!chestData.contains("chests." + chestName)) {
			player.sendMessage(MessageManager.get("messages.invalid_delete", "name", chestName));
			return;
		}

		List<Map<?, ?>> locations = chestData.getMapList("chests." + chestName + ".locations");
		int removedCount = 0;

		if (locations != null) {
			for (Map<?, ?> rawLoc : locations) {
				Location loc = Location.deserialize((Map<String, Object>) rawLoc);

				// 🧱 Odstranit blok
				if (loc.getBlock().getType() == Material.CHEST) {
					loc.getBlock().setType(Material.AIR);
				}

				// 🗑️ Smazat hologram
				HologramManager.removeHologram(loc);
				removedCount++;
			}
		}

		// 🗂️ Smazat z configu
		chestData.set("chests." + chestName, null);
		NerKubLootChests.getInstance().saveChestData();

		player.sendMessage(MessageManager.get("messages.deleted", "name", chestName, "count", String.valueOf(removedCount)));
	}

	private void handleAdd(Player player, String[] args) {
		if (args.length < 3) {
			player.sendMessage(MessageManager.get("messages.usage_add"));
			return;
		}

		String chestName = args[1].toLowerCase();
		int chance;

		try {
			chance = Integer.parseInt(args[2]);
			if (chance < 0 || chance > 100) throw new NumberFormatException();
		} catch (NumberFormatException e) {
			player.sendMessage(MessageManager.get("messages.invalid_chance"));
			return;
		}

		ItemStack item = player.getInventory().getItemInMainHand();
		if (item == null || item.getType() == Material.AIR) {
			player.sendMessage(MessageManager.get("messages.must_hold_item"));
			return;
		}

		var chestData = NerKubLootChests.getInstance().getChestData();

		if (!chestData.contains("chests." + chestName)) {
			player.sendMessage(MessageManager.get("messages.chest_not_found", "name", chestName));
			return;
		}

		String base64 = cz.nerkub.nerKubLootChests.utils.ItemSerializer.itemToBase64(item);


		List<Map<String, Object>> items = (List<Map<String, Object>>) chestData.getList("chests." + chestName + ".items");
		if (items == null) items = new ArrayList<>();

		Map<String, Object> entry = new HashMap<>();
		entry.put("chance", chance);
		entry.put("item", base64);
		items.add(entry);

		chestData.set("chests." + chestName + ".items", items);
		NerKubLootChests.getInstance().saveChestData();

		player.sendMessage(MessageManager.get("messages.added_item", "name", chestName, "chance", String.valueOf(chance)));
	}

	private void handleLootTime(Player player, String[] args) {
		if (args.length < 3) {
			player.sendMessage(MessageManager.get("messages.usage_loottime"));
			return;
		}

		String chestName = args[1].toLowerCase();
		int seconds;

		try {
			seconds = Integer.parseInt(args[2]);
			if (seconds <= 0) throw new NumberFormatException();
		} catch (NumberFormatException e) {
			player.sendMessage(MessageManager.get("messages.invalid_time"));
			return;
		}

		var chestData = NerKubLootChests.getInstance().getChestData();
		if (!chestData.contains("chests." + chestName)) {
			player.sendMessage(MessageManager.get("messages.chest_not_found", "name", chestName));
			return;
		}

		chestData.set("chests." + chestName + ".refreshTime", seconds);
		NerKubLootChests.getInstance().saveChestData();

		player.sendMessage(MessageManager.get("messages.time_updated", "name", chestName, "time", String.valueOf(seconds)));
	}

	private void handleGet(Player player, String[] args) {
		if (args.length < 2) {
			player.sendMessage(MessageManager.get("messages.usage_get"));
			return;
		}

		String chestName = args[1].toLowerCase();
		YamlConfiguration chestData = NerKubLootChests.getInstance().getChestData();

		if (!chestData.contains("chests." + chestName)) {
			player.sendMessage(MessageManager.get("messages.chest_not_found", "name", chestName));
			return;
		}

		String matName = chestData.getString("chests." + chestName + ".blockType", Material.CHEST.name());
		Material blockType = Material.matchMaterial(matName);

		if (blockType == null || !SupportedContainers.VALID_CONTAINERS.contains(blockType)) {
			blockType = Material.CHEST; // fallback
		}

		ItemStack item = new ItemStack(blockType);
		ItemMeta meta = item.getItemMeta();

		if (meta != null) {
			meta.setDisplayName(ChatColor.GOLD + "LootChest: " + chestName);
			NamespacedKey key = new NamespacedKey(NerKubLootChests.getInstance(), "lootchest");
			meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, chestName);
			item.setItemMeta(meta);
			player.getInventory().addItem(item);
			player.sendMessage(MessageManager.get("messages.given", "name", chestName));
		} else {
			player.sendMessage("§cChyba: Nelze vytvořit předmět pro typ: " + blockType.name());
		}
	}


	private void handleEdit(Player player, String[] args) {
		if (args.length < 2) {
			player.sendMessage(MessageManager.get("messages.usage_edit"));
			return;
		}

		String chestName = args[1].toLowerCase();
		var chestData = NerKubLootChests.getInstance().getChestData();

		if (!chestData.contains("chests." + chestName)) {
			player.sendMessage(MessageManager.get("messages.chest_not_found", "name", chestName));
			return;
		}

		// 👁️ Otevři GUI
		EditChestMenu.open(player, chestName);
	}

	public static void deleteChest(Player player, String chestName) {
		var chestData = NerKubLootChests.getInstance().getChestData();

		if (!chestData.contains("chests." + chestName)) {
			player.sendMessage(MessageManager.get("messages.not_found", "name", chestName));
			return;
		}

		List<Map<?, ?>> locations = chestData.getMapList("chests." + chestName + ".locations");
		int removedCount = 0;

		if (locations != null) {
			for (Map<?, ?> rawLoc : locations) {
				Location loc = Location.deserialize((Map<String, Object>) rawLoc);
				if (loc.getBlock().getType() == Material.CHEST) {
					loc.getBlock().setType(Material.AIR);
				}
				HologramManager.removeHologram(loc);
				removedCount++;
			}
		}

		chestData.set("chests." + chestName, null);
		NerKubLootChests.getInstance().saveChestData();

		// 🔥 Remove from inventory
		NamespacedKey key = new NamespacedKey(NerKubLootChests.getInstance(), "lootchest");

		for (ItemStack item : player.getInventory().getContents()) {
			if (item == null || item.getType().isAir()) continue;
			if (!item.hasItemMeta()) continue;

			var meta = item.getItemMeta();
			if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) continue;

			String tag = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
			if (tag != null && tag.equalsIgnoreCase(chestName)) {
				player.getInventory().remove(item);
			}
		}

		player.sendMessage(MessageManager.get("messages.deleted", "name", chestName, "count", String.valueOf(removedCount)));
	}

	private void handleReload(Player player) {
		NerKubLootChests.getInstance().reloadConfig(); // config.yml
		NerKubLootChests.getInstance().reloadChestsFile(); // chests.yml
		MessageManager.init(NerKubLootChests.getInstance()); // messages.yml

		player.sendMessage(MessageManager.get("messages.reloaded"));
	}



}
