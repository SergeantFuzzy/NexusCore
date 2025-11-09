package dev.sergeantfuzzy.NexusCore.Abilities;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@FunctionalInterface
public interface AbilityAction {
    void execute(JavaPlugin plugin, Player player);
}
