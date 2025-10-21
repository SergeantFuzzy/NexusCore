package dev.sergeantfuzzy.NexusCore.Commands;

import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class SimpleRegisteredCommand extends Command implements PluginIdentifiableCommand {
    private final Plugin plugin;
    private final CommandBase handler;
    SimpleRegisteredCommand(Plugin plugin, String name, String description, String[] aliases, CommandBase handler) {
        super(name);
        this.plugin = plugin;
        this.handler = handler;
        if (description != null) setDescription(description);
        if (aliases != null && aliases.length > 0) setAliases(Arrays.asList(aliases));
        setUsage("/" + name);
    }
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        try {
            return handler.onCommand(sender, this, label, args);
        } catch (Throwable t) {
            plugin.getLogger().warning("Command '/" + getName() + "' failed: " + t.getMessage());
            return true;
        }
    }
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        try {
            List<String> out = handler.onTabComplete(sender, this, alias, args);
            return out != null ? out : Collections.emptyList();
        } catch (Throwable ignored) {
            return Collections.emptyList();
        }
    }
    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}