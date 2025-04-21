package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.utils.ChestUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

public class AntiExplosionListener implements Listener {

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		removeProtectedChests(event.blockList().iterator());
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event) {
		removeProtectedChests(event.blockList().iterator());
	}

	private void removeProtectedChests(Iterator<Block> iterator) {
		while (iterator.hasNext()) {
			Block block = iterator.next();
			if (block.getType() != Material.CHEST) continue;

			if (!(block.getState() instanceof Chest chest)) continue;

			Location loc = chest.getLocation();
			if (ChestUtils.getChestNameAtLocation(loc) != null) {
				iterator.remove(); // 🛡️ chráníme loot chestku před zničením
			}
		}
	}
}
