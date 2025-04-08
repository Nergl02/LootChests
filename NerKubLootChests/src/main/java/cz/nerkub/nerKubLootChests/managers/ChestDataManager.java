package cz.nerkub.nerKubLootChests.managers;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ChestDataManager {

	private static final Map<Player, BiConsumer<Player, String>> chatInputHandlers = new HashMap<>();

	public static void awaitDisplayNameInput(Player player, String chestName) {
		chatInputHandlers.put(player, (p, msg) -> {
			String path = "chests." + chestName + ".displayName";
			NerKubLootChests.getInstance().getChestData().set(path, msg);
			NerKubLootChests.getInstance().saveChestData();

			p.sendMessage(MessageManager.get("messages.display_name_set", "value", msg));

			Bukkit.getScheduler().runTask(NerKubLootChests.getInstance(), () ->
					HologramManager.reloadAll());
		});
	}

	public static void awaitRefreshTimeInput(Player player, String chestName) {
		chatInputHandlers.put(player, (p, msg) -> {
			try {
				int seconds = Integer.parseInt(msg);
				if (seconds <= 0) throw new NumberFormatException();

				String path = "chests." + chestName + ".refreshTime";
				NerKubLootChests.getInstance().getChestData().set(path, seconds);
				NerKubLootChests.getInstance().saveChestData();

				p.sendMessage(MessageManager.get("messages.refresh_time_set", "time", String.valueOf(seconds)));

				Bukkit.getScheduler().runTask(NerKubLootChests.getInstance(), () ->
						HologramManager.reloadAll());

			} catch (NumberFormatException e) {
				p.sendMessage(MessageManager.get("messages.invalid_time_input"));
			}
		});
	}

	public static void awaitItemsPerRefreshInput(Player player, String chestName) {
		chatInputHandlers.put(player, (p, msg) -> {
			try {
				int count = Integer.parseInt(msg);
				if (count <= 0) throw new NumberFormatException();

				String path = "chests." + chestName + ".itemsPerRefresh";
				NerKubLootChests.getInstance().getChestData().set(path, count);
				NerKubLootChests.getInstance().saveChestData();

				p.sendMessage(MessageManager.get("messages.items_per_refresh_set", "amount", String.valueOf(count)));

				Bukkit.getScheduler().runTask(NerKubLootChests.getInstance(), () ->
						HologramManager.reloadAll());

			} catch (NumberFormatException e) {
				p.sendMessage(MessageManager.get("messages.invalid_item_amount"));
			}
		});
	}

	public static boolean handleChatInput(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (!chatInputHandlers.containsKey(player)) return false;

		event.setCancelled(true);
		String msg = event.getMessage();

		BiConsumer<Player, String> handler = chatInputHandlers.remove(player);
		if (handler != null) handler.accept(player, msg);
		return true;
	}
}
