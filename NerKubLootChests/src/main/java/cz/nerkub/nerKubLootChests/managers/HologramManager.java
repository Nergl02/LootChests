package cz.nerkub.nerKubLootChests.managers;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class HologramManager {

	private static final Map<Location, ArmorStand> HOLOGRAM_CACHE = new HashMap<>();

	public static void createOrUpdateCountdownHologram(String chestName, Location loc, int secondsLeft) {
		// 🛡️ Zamez zobrazení odpočtu s 0 nebo zápornou hodnotou
		if (secondsLeft <= 1) return;

		String displayName = getDisplayNameFromChest(chestName);
		String formattedTime = formatTime(secondsLeft);

		String line = NerKubLootChests.getInstance().getConfig().getString("hologram-format",
						"&e%displayName% &7(obnova za &a%timeLeftFormatted%&7)")
				.replace("%displayName%", displayName)
				.replace("%timeLeftFormatted%", formattedTime);

		spawnOrUpdateHologram(loc, color(line));
	}


	public static void createOrUpdateLootableHologram(String chestName, Location loc, int itemCount) {
		String displayName = getDisplayNameFromChest(chestName);
		String line = NerKubLootChests.getInstance().getConfig().getString("hologram-filled-format",
						"&aVylootuj! &7(&f%itemsLeft% items&7)")
				.replace("%displayName%", displayName)
				.replace("%itemsLeft%", String.valueOf(itemCount));

		spawnOrUpdateHologram(loc, color(line));
	}

	private static void spawnOrUpdateHologram(Location location, String customName) {
		Location key = getBaseLocationKey(location);
		ArmorStand current = HOLOGRAM_CACHE.get(key);

		// Check if exists and alive
		if (current == null || current.isDead() || !current.isValid()) {
			removeNearbyArmorStands(location);

			Location holoLoc = location.clone().add(0.5, 1.0, 0.5);
			ArmorStand armor = (ArmorStand) location.getWorld().spawnEntity(holoLoc, EntityType.ARMOR_STAND);

			armor.setCustomName(customName);
			armor.setCustomNameVisible(true);
			armor.setInvisible(true);
			armor.setMarker(true);
			armor.setSmall(true);
			armor.setGravity(false);

			HOLOGRAM_CACHE.put(key, armor);
			if (NerKubLootChests.getInstance().getConfig().getBoolean("debug")) {
				System.out.println("✅ [Hologram] New hologram created at: " + key);
			}
		} else {
			current.setCustomName(customName);
		}
	}

	public static void removeHologram(Location location) {
		Location key = getBaseLocationKey(location);
		ArmorStand armor = HOLOGRAM_CACHE.remove(key);

		if (armor != null && !armor.isDead()) {
			armor.remove();
			if (NerKubLootChests.getInstance().getConfig().getBoolean("debug")) {
			System.out.println("🧹 [Hologram] ArmorStand removed from cache and world: " + key);
			}
		}

		removeNearbyArmorStands(location);
	}

	private static void removeNearbyArmorStands(Location loc) {
		int removed = 0;
		for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1.0, 1.5, 1.0)) {
			if (entity instanceof ArmorStand armor) {
				if (armor.isCustomNameVisible()) {
					armor.remove();
					removed++;
				}
			}
		}
		if (NerKubLootChests.getInstance().getConfig().getBoolean("debug")) {
			if (removed > 0) System.out.println("🧹 [Hologram] Removed from near distance: " + removed);
		}
	}

	public static void clearAll() {
		HOLOGRAM_CACHE.values().forEach(Entity::remove);
		HOLOGRAM_CACHE.clear();
		if (NerKubLootChests.getInstance().getConfig().getBoolean("debug")) {
			System.out.println("🧹 [Hologram] All holograms removed when launching plugin.");
		}
	}

	public static void reloadAll() {
		var data = NerKubLootChests.getInstance().getChestData();
		if (!data.isConfigurationSection("chests")) return;

		long now = System.currentTimeMillis() / 1000;

		for (String chestName : data.getConfigurationSection("chests").getKeys(false)) {
			ConfigurationSection section = data.getConfigurationSection("chests." + chestName);
			if (section == null) continue;

			int refresh = section.getInt("refreshTime", 300);
			long last = section.getLong("lastRefreshed", 0);
			int left = (int) (refresh - (now - last));
			List<Map<?, ?>> locationMaps = section.getMapList("locations");

			for (Map<?, ?> map : locationMaps) {
				Location loc = Location.deserialize((Map<String, Object>) map);
				if (loc.getWorld() == null) continue;

				Block block = loc.getBlock();
				if (!cz.nerkub.nerKubLootChests.SupportedContainers.VALID_CONTAINERS.contains(block.getType())) continue;
				if (!(block.getState() instanceof org.bukkit.block.Container container)) continue;

				Inventory inv = container.getInventory();
				boolean hasItems = Arrays.stream(inv.getContents())
						.filter(Objects::nonNull)
						.anyMatch(item -> item.getType() != Material.AIR);

				if (hasItems) {
					int count = (int) Arrays.stream(inv.getContents())
							.filter(Objects::nonNull)
							.filter(item -> item.getType() != Material.AIR)
							.count();
					createOrUpdateLootableHologram(chestName, loc, count);
				} else {
					createOrUpdateCountdownHologram(chestName, loc, Math.max(left, 0));
				}
			}
		}

		if (NerKubLootChests.getInstance().getConfig().getBoolean("debug")) {
			System.out.println("♻️ [Hologram] Reload all holograms.");
		}
	}

	private static String getDisplayNameFromChest(String chestName) {
		String raw = NerKubLootChests.getInstance().getChestData()
				.getString("chests." + chestName + ".displayName", chestName);
		return ChatColor.translateAlternateColorCodes('&', raw);
	}

	private static String color(String text) {
		return ChatColor.translateAlternateColorCodes('&', text);
	}

	private static String formatTime(int totalSeconds) {
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;

		if (hours > 0) return String.format("%dh %02dm", hours, minutes);
		if (minutes > 0) return String.format("%dm %02ds", minutes, seconds);
		return String.format("%ds", seconds);
	}

	private static Location getBaseLocationKey(Location loc) {
		return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	@Deprecated
	public static boolean isFromPluginChest(Chest chest) {
		if (chest == null) return false;
		var container = chest.getPersistentDataContainer();
		var key = new NamespacedKey(NerKubLootChests.getInstance(), "lootchest");
		return container.has(key, PersistentDataType.STRING);
	}


	public static void spawnCountdown(Location loc, String chestName, int timeLeft) {
		String displayName = getDisplayNameFromChest(chestName);
		createOrUpdateCountdownHologram(chestName, loc, timeLeft);
		if (NerKubLootChests.getInstance().getConfig().getBoolean("debug")) {
			System.out.println("🔄 [Hologram] Countdown refreshed for '" + chestName + "' at location: " + loc);
		}
	}


}
