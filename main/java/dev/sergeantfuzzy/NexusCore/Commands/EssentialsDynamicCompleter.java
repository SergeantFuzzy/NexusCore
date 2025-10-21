package dev.sergeantfuzzy.NexusCore.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public final class EssentialsDynamicCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        EssentialsTabRegistry.Provider p = EssentialsTabRegistry.get(cmd.getName());
        if (p == null) return Collections.emptyList();
        List<String> out = p.complete(sender, alias, args);
        return (out == null) ? Collections.emptyList() : out;
    }
}