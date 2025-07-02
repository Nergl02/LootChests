package cz.nerkub.nerKubLootChests.listeners;

import cz.nerkub.nerKubLootChests.SupportedContainers;
import cz.nerkub.nerKubLootChests.utils.ChestUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

public class AntiExplosionListener implements Listener {

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		removeProtectedLootContainers(event.blockList().iterator());
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event) {
		removeProtectedLootContainers(event.blockList().iterator());
	}

	private void removeProtectedLootContainers(Iterator<Block> iterator) {
		while (iterator.hasNext()) {
			Block block = iterator.next();

			// 🎯 Kontrola, zda blok je typ kontejneru (CHEST, BARREL, SHULKER_BOX...)
			if (!SupportedContainers.VALID_CONTAINERS.contains(block.getType())) continue;

			Location loc = block.getLocation();

			// 🔒 Pokud je v chests.yml zaregistrován, chráníme ho
			if (ChestUtils.getChestNameAtLocation(loc) != null) {
				iterator.remove();
			}
		}
	}
}
