package cz.nerkub.nerKubLootChests;

import cz.nerkub.nerKubLootChests.commands.LootChestCommand;
import cz.nerkub.nerKubLootChests.commands.LootChestTabCompleter;
import cz.nerkub.nerKubLootChests.gui.EditChestMenu;
import cz.nerkub.nerKubLootChests.listeners.*;
import cz.nerkub.nerKubLootChests.managers.HologramManager;
import cz.nerkub.nerKubLootChests.managers.HologramUpdater;
import cz.nerkub.nerKubLootChests.managers.MessageManager;
import cz.nerkub.nerKubLootChests.tasks.ChestRefresher;
import cz.nerkub.nerKubLootChests.tasks.LootChecker;
import cz.nerkub.nerKubLootChests.utils.YamlUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class NerKubLootChests extends JavaPlugin {

	private static NerKubLootChests plugin;
	private YamlConfiguration chestData;
	private File chestFile;
	private CheckUpdatesGitHub checkUpdatesGitHub;

	@Override
	public void onEnable() {
		plugin = this;

		saveDefaultConfig();
		setupChestFile();

		// 📦 Commands
		getCommand("lootchest").setExecutor(new LootChestCommand());
		getCommand("lootchest").setTabCompleter(new LootChestTabCompleter());

		YamlUpdater.update(new File(getDataFolder(), "config.yml"), this);
		YamlUpdater.update(new File(getDataFolder(), "messages.yml"), this);

		// 📌 Events
		getServer().getPluginManager().registerEvents(new ChestPlaceBreakListener(), this);
		getServer().getPluginManager().registerEvents(new ChestInteractListener(), this);
		Bukkit.getPluginManager().registerEvents(new EditChestMenu(), this);
		Bukkit.getPluginManager().registerEvents(new EditChestMenuListener(), this);
		Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
		getServer().getPluginManager().registerEvents(new ConfirmDeleteMenuListener(), this);
		getServer().getPluginManager().registerEvents(new AddLootItemsListener(), this);
		getServer().getPluginManager().registerEvents(new EditLootItemsListener(), this);
		getServer().getPluginManager().registerEvents(new ChanceEditorListener(), this);
		getServer().getPluginManager().registerEvents(new AntiExplosionListener(), this);
		getServer().getPluginManager().registerEvents(new BlockTypeSelectorMenuListener(), this);

		MessageManager.init(this);

		Bukkit.getConsoleSender().sendMessage("");
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3|\\   |  | /	&aPlugin: &6NerKub LootChests"));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3| \\  |  |/	&aVersion: &bv1.0.5"));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3|  \\ |  |\\	&aAuthor: &3NerKub Studio"));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3|   \\|  | \\	&aPremium: &bThis plugin is a premium resource."));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', " "));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3| Visit our Discord for more! &ahttps://discord.gg/YXm26egK6g"));
		Bukkit.getConsoleSender().sendMessage("");

		checkUpdatesGitHub = new CheckUpdatesGitHub(this);
		checkUpdatesGitHub.checkForUpdates();
		getServer().getPluginManager().registerEvents(new JoinListener(checkUpdatesGitHub), this);

		// 🧠 Task schedulers
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new ChestRefresher(), 20L, 20L);     // každou sekundu
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new HologramUpdater(), 20L, 20L);    // každou sekundu
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new LootChecker(), 20L, 40L);        // každé 2s

		// ⏳ Počkej na načtení světa a pak načti hologramy
		Bukkit.getScheduler().runTaskLater(this, () -> {
			HologramManager.clearAll();            // 🧹 pro jistotu odstraní staré
			HologramManager.reloadAll();           // ♻️ znovu spawnuje hologramy
		}, 20L); // počkej 1 sekundu (20 ticků)

	}

	@Override
	public void onDisable() {
		HologramManager.clearAll(); // 🧹 Odstraň všechny hologramy
		getLogger().info("🛑 LootChests plugin off.");
	}

	public void reloadChestsFile() {
		setupChestFile();
	}


	public void setupChestFile() {
		chestFile = new File(getDataFolder(), "chests.yml");
		if (!chestFile.exists()) {
			saveResource("chests.yml", false);
		}
		chestData = YamlConfiguration.loadConfiguration(chestFile);
	}

	// 📡 Statické metody
	public static NerKubLootChests getInstance() {
		return plugin;
	}

	public YamlConfiguration getChestData() {
		return chestData;
	}

	public File getChestFile() {
		return chestFile;
	}

	public void saveChestData() {
		try {
			chestData.save(chestFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
