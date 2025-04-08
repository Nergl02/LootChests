package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.managers.ChestDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		ChestDataManager.handleChatInput(event);
	}
}
