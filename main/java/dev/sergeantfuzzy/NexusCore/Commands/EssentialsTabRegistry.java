package dev.sergeantfuzzy.NexusCore.Commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class EssentialsTabRegistry {
    private EssentialsTabRegistry() {}
    private static final Map<String, Provider> providers = new ConcurrentHashMap<>();
    @FunctionalInterface
    public interface Provider {
        List<String> complete(CommandSender sender, String alias, String[] args);
    }
    public static void register(String commandName, Provider provider) {
        providers.put(commandName.toLowerCase(Locale.ROOT), provider);
    }
    public static Provider get(String commandName) {
        return providers.get(commandName.toLowerCase(Locale.ROOT));
    }
    public static List<String> matchPrefix(Collection<String> items, String prefix) {
        String p = (prefix == null ? "" : prefix).toLowerCase(Locale.ROOT);
        if (p.isEmpty()) return new ArrayList<>(items);
        List<String> out = new ArrayList<>();
        for (String s : items) if (s.toLowerCase(Locale.ROOT).startsWith(p)) out.add(s);
        return out;
    }
    public static List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
        return names;
    }
    public static List<String> boolYesNo() { return List.of("true", "false", "yes", "no", "on", "off"); }
    public static List<String> ints(int min, int max) {
        List<String> out = new ArrayList<>();
        for (int i = min; i <= max; i++) out.add(Integer.toString(i));
        return out;
    }
}