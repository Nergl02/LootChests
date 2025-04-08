package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.CheckUpdatesGitHub;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

	private final CheckUpdatesGitHub updateChecker;

	public JoinListener(CheckUpdatesGitHub updateChecker) {
		this.updateChecker = updateChecker;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		updateChecker.notifyPlayerOnJoin(event.getPlayer());
	}
}