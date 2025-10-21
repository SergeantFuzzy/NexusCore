package dev.sergeantfuzzy.NexusCore.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class LeaveListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // Reserved for cleanup/metrics if needed later.
    }
}