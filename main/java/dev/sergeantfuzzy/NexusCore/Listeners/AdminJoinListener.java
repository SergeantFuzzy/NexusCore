package dev.sergeantfuzzy.NexusCore.Listeners;

import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.UpdateChecker;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdminJoinListener implements Listener {
    private final JavaPlugin plugin;
    private final UpdateChecker updateChecker;
    public AdminJoinListener(JavaPlugin plugin, int spigotResourceId, String builtByBitUrl) {
        this.plugin = plugin;
        this.updateChecker = new UpdateChecker(plugin, spigotResourceId, builtByBitUrl);
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!(p.isOp() || p.hasPermission("nexuscore.admin"))) return;
        Component info = Msg.buildAdminJoinLine(plugin);
        Msg.sendPrefixed(p, info);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> updateChecker.checkAndNotify(p));
    }
}