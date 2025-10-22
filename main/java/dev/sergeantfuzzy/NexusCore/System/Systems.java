package dev.sergeantfuzzy.NexusCore.System;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Systems {
    private static JavaPlugin plugin;
    private static volatile boolean bootstrapped = false;

    static { bootstrap(); }
    private Systems() {}
    private static void bootstrap() {
        if (bootstrapped) return;
        try {
            plugin = JavaPlugin.getProvidingPlugin(Systems.class);
        } catch (Throwable ignored) {
            Plugin p = Bukkit.getPluginManager().getPlugin("NexusCore");
            if (p instanceof JavaPlugin jp) plugin = jp;
        }
        if (plugin == null) return;
        Bukkit.getScheduler().runTask(plugin, Systems::loadEnabledFeatures);
        bootstrapped = true;
    }
    public static void loadEnabledFeatures() {
        if (plugin == null) return;
        boolean sb = plugin.getConfig().getBoolean("System.Scoreboard", true);
        boolean tl = plugin.getConfig().getBoolean("System.Tablist", true);
        if (sb) touch("dev.sergeantfuzzy.NexusCore.System.Scoreboard.Scoreboard");
        if (tl) touch("dev.sergeantfuzzy.NexusCore.System.Tablist.Tablist");
    }
    private static void touch(String className) {
        try {
            ClassLoader cl = (plugin != null)
                    ? plugin.getClass().getClassLoader()
                    : Systems.class.getClassLoader(); // fallback
            if (cl == null) cl = Thread.currentThread().getContextClassLoader();
            Class.forName(className, true, cl);
        } catch (Throwable t) {
            if (plugin != null) {
                plugin.getLogger().warning("[Systems] Failed to load " + className + ": " + t.getMessage());
            } else {
                Bukkit.getLogger().warning("[Systems] Failed to load " + className + ": " + t.getMessage());
            }
        }
    }
}