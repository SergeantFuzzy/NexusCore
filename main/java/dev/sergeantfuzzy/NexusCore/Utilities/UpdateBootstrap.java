package dev.sergeantfuzzy.NexusCore.Utilities;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public final class UpdateBootstrap implements Listener {
    private static final int CHECK_PERIOD_TICKS = 12 * 60 * 60 * 20;
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final JavaPlugin plugin;
    private final UpdateChecker checker;
    private BukkitTask task;
    private UpdateBootstrap(JavaPlugin plugin, UpdateChecker checker) {
        this.plugin = plugin;
        this.checker = checker;
    }
    public static UpdateBootstrap enable(JavaPlugin plugin, UpdateChecker checker) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(checker, "checker");
        if (!plugin.getConfig().getBoolean("Update Check", true)) {
            return new UpdateBootstrap(plugin, checker);
        }
        UpdateBootstrap ub = new UpdateBootstrap(plugin, checker);
        Bukkit.getPluginManager().registerEvents(ub, plugin);
        CommandSender console = plugin.getServer().getConsoleSender();
        ub.safeCheck(console);
        ub.task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin, () -> ub.safeCheck(console), CHECK_PERIOD_TICKS, CHECK_PERIOD_TICKS);
        return ub;
    }
    public void disable() {
        HandlerList.unregisterAll(this);
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
    public void refresh() {
        boolean enabled = plugin.getConfig().getBoolean("Update Check", true);
        if (!enabled) {
            disable();
            return;
        }
        if (task == null) {
            UpdateBootstrap ub = enable(plugin, checker);
            this.task = ub.task;
        }
    }
    private void safeCheck(CommandSender target) {
        try {
            checker.checkAndNotify(target);
        } catch (Throwable t) {
            plugin.getLogger().fine("Update check skipped: " + t.getMessage());
        }
    }
}