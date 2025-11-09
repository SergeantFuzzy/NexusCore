package dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Movement;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Util.GameplayCommandUtil.*;

public final class BackCommand implements CommandExecutor, Listener {
    private static final String PERM_SELF = "nexuscore.back";
    private static final String PERM_OTHERS = "nexuscore.back.others";
    private static final int MAX_HISTORY = 10;
    private final JavaPlugin plugin;
    private final Map<UUID, Deque<Location>> history = new ConcurrentHashMap<>();

    public BackCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!sender.hasPermission(PERM_SELF)) {
                sendPrefixedMM(sender, "<red>You don't have permission.</red>");
                return true;
            }
            if (!ensurePlayerSender(sender)) return true;
            Player player = (Player) sender;
            teleportBack(player, sender);
            return true;
        }

        if (!sender.hasPermission(PERM_OTHERS)) {
            sendPrefixedMM(sender, "<red>You don't have permission to send others back.</red>");
            return true;
        }

        Optional<Player> opt = findPlayer(args[0]);
        if (opt.isEmpty()) {
            sendPrefixedMM(sender, "<red>Player <b><name></b> is not online.</red>",
                    Placeholder.unparsed("name", args[0]));
            return true;
        }
        teleportBack(opt.get(), sender);
        return true;
    }

    private void teleportBack(Player target, CommandSender actor) {
        Optional<Location> destOpt = consume(target);
        if (destOpt.isEmpty()) {
            if (actor.equals(target)) {
                sendPrefixedMM(actor, "<red>You don't have a previous location saved.</red>");
            } else {
                sendPrefixedMM(actor, "<red><b><name></b> doesn't have a previous location saved.</red>",
                        Placeholder.unparsed("name", target.getName()));
            }
            return;
        }
        Location dest = destOpt.get();
        boolean success = target.teleport(dest);
        if (!success) {
            restore(target.getUniqueId(), dest);
            plugin.getLogger().warning("Failed to teleport " + target.getName() + " to a saved /back location.");
            sendPrefixedMM(actor, "<red>Teleport failed. Try again.</red>");
            return;
        }
        if (!actor.equals(target)) {
            sendPrefixedMM(actor, "<green>Teleported <b><name></b> to their previous location.</green>" + actionBar(
                            runAction("Back Again", "/back " + target.getName(), "Step them further back"),
                            runAction("Spawn", "/spawn " + target.getName(), "Send them to spawn"),
                            suggestAction("TP Player", "/tp ", "Type someone to visit")
                    ),
                    Placeholder.unparsed("name", target.getName()));
            sendPrefixedMM(target, "<green>You were sent back by <b><name></b>.</green>" + actionBar(
                            runAction("Back Again", "/back", "Step back once more"),
                            runAction("Spawn", "/spawn", "Head to spawn"),
                            suggestAction("TP Player", "/tp ", "Type someone to visit")
                    ),
                    Placeholder.unparsed("name", actor.getName()));
        } else {
            sendPrefixedMM(target, "<green>Returned to your previous location.</green>" +
                    actionBar(
                            runAction("Back Again", "/back", "Walk back the history"),
                            runAction("Spawn", "/spawn", "Go to spawn"),
                            suggestAction("TP Player", "/tp ", "Type someone to visit")
                    ));
        }
    }

    private Optional<Location> consume(Player player) {
        if (player == null) return Optional.empty();
        return consume(player.getUniqueId());
    }

    private Optional<Location> consume(UUID uuid) {
        Deque<Location> stack = history.get(uuid);
        if (stack == null || stack.isEmpty()) return Optional.empty();
        Location loc = stack.pollFirst();
        if (stack.isEmpty()) history.remove(uuid);
        return loc == null ? Optional.empty() : Optional.of(loc.clone());
    }

    private void remember(Player player, Location location) {
        if (player == null) return;
        remember(player.getUniqueId(), location);
    }

    private void remember(UUID uuid, Location raw) {
        if (uuid == null || raw == null || raw.getWorld() == null) return;
        Deque<Location> stack = history.computeIfAbsent(uuid, __ -> new ArrayDeque<>());
        stack.addFirst(raw.clone());
        while (stack.size() > MAX_HISTORY) stack.removeLast();
    }

    private void restore(UUID uuid, Location loc) {
        remember(uuid, loc);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        remember(event.getPlayer(), event.getFrom());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        remember(event.getEntity(), event.getEntity().getLocation());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        history.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        history.remove(event.getPlayer().getUniqueId());
    }
}
