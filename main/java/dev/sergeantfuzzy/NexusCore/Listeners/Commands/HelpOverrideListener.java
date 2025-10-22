package dev.sergeantfuzzy.NexusCore.Listeners.Commands;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class HelpOverrideListener implements Listener {
    private final JavaPlugin plugin;
    public HelpOverrideListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    private static String normalize(String in, boolean stripLeadingSlash) {
        String s = in == null ? "" : in.trim().toLowerCase(Locale.ROOT);
        if (stripLeadingSlash && s.startsWith("/")) s = s.substring(1);
        s = s.replaceAll("\\s+", " ");
        return s;
    }
    private static boolean isTarget(String normalizedNoSlash) {
        String[] parts = normalizedNoSlash.split(" ");
        if (parts.length < 2) return false;
        String verb = parts[0];
        if (!verb.equals("help") && !verb.equals("?")) return false;
        String name = parts[1];
        boolean matchesName = name.equals("nexuscore") || name.equals("nexus-core") || name.equals("nexus") || (name.equals("nexus") && parts.length >= 3 && parts[2].equals("core")) || (name.equals("nexus") && parts.length >= 2);
        boolean nameIsTwoTokens = (name.equals("nexus") && parts.length >= 3 && parts[2].equals("core"));
        return name.equals("nexuscore")
                || name.equals("nexus-core")
                || nameIsTwoTokens;
    }
    private static String extractSectionOrDefault(String normalizedNoSlash) {
        String[] p = normalizedNoSlash.split(" ");
        if (p.length >= 3 && (p[1].equals("nexuscore") || p[1].equals("nexus-core"))) {
            return p[2];
        }
        if (p.length >= 4 && p[1].equals("nexus") && p[2].equals("core")) {
            return p[3];
        }
        return "help";
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHelp(PlayerCommandPreprocessEvent e) {
        String norm = normalize(e.getMessage(), false);
        String normNoSlash = normalize(e.getMessage(), true);
        if (!isTarget(normNoSlash)) return;
        e.setCancelled(true);
        String section = extractSectionOrDefault(normNoSlash);
        switch (section) {
            case "help", "home", "commands", "perms", "permissions", "links" -> {
                e.getPlayer().performCommand("nexushelp " + section);
            }
            default -> e.getPlayer().performCommand("nexushelp help");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsoleHelp(ServerCommandEvent e) {
        String normNoSlash = normalize(e.getCommand(), true);
        if (!isTarget(normNoSlash)) return;
        e.setCancelled(true);
        String section = extractSectionOrDefault(normNoSlash);
        String routed = switch (section) {
            case "help", "home", "commands", "perms", "permissions", "links" -> "nexushelp " + section;
            default -> "nexushelp help";
        };
        plugin.getServer().dispatchCommand(e.getSender(), routed);
    }
}