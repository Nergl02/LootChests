package cz.nerkub.nerKubLootChests.commands;

import cz.nerkub.nerKubLootChests.NerKubLootChests;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LootChestTabCompleter implements TabCompleter {

	private static final List<String> SUBCOMMANDS = List.of("create", "add", "loottime", "delete", "get", "edit", "reload");

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
									  @NotNull String alias, @NotNull String[] args) {

		if (args.length == 1) {
			return SUBCOMMANDS.stream()
					.filter(s -> s.startsWith(args[0].toLowerCase()))
					.collect(Collectors.toList());
		}

		if (args.length == 2) {
			String sub = args[0].toLowerCase();
			if (List.of("add", "delete", "loottime", "get", "edit").contains(sub)) {
				YamlConfiguration chestData = NerKubLootChests.getInstance().getChestData();
				if (!chestData.isConfigurationSection("chests")) return Collections.emptyList();

				return new ArrayList<>(chestData.getConfigurationSection("chests").getKeys(false)).stream()
						.filter(s -> s.startsWith(args[1].toLowerCase()))
						.collect(Collectors.toList());
			}
		}

		return Collections.emptyList();
	}
}
