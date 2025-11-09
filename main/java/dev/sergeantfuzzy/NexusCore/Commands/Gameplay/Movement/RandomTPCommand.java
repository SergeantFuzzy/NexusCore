package dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Movement;

import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Util.GameplayCommandUtil.*;

public final class RandomTPCommand implements CommandExecutor, TabCompleter {
    private static final String PERM_SELF   = "nexuscore.rtp";
    private static final String PERM_OTHERS = "nexuscore.rtp.others";
    private final JavaPlugin plugin;
    public RandomTPCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        // (reserved for future: config hooks, cooldowns, etc.)
    }
    private static final int MAX_TRIES = 40;
    private static final int MIN_RADIUS = 256;
    private static final int MAX_RADIUS = 2048;
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!sender.hasPermission(PERM_SELF) || !ensurePlayerSender(sender)) return true;
            Player p = (Player) sender;
            if (rtp(p)) {
                sendPrefixedMM(sender, "<green>Teleported to a random safe location.</green> " +
                        choiceBar("/rtp", "Random teleport again"));
            } else {
                sendPrefixedMM(sender, "<red>Failed to find a safe spot. Try again.</red>");
            }
            return true;
        }
        if (!sender.hasPermission(PERM_OTHERS)) {
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Only admins may random-teleport others.</red>");
            return true;
        }
        Optional<Player> opt = findPlayer(args[0]);
        if (opt.isEmpty()) {
            sendPrefixedMM(sender, "<red>Player <b><name></b> is not online.</red>",
                    Placeholder.unparsed("name", args[0]));
            return true;
        }
        Player t = opt.get();
        if (rtp(t)) {
            sendPrefixedMM(sender, "<green>Random-teleported <b><name></b>.</green>",
                    Placeholder.unparsed("name", t.getName()));
        } else {
            sendPrefixedMM(sender, "<red>Failed to find safe spot for <b><name></b>.</red>",
                    Placeholder.unparsed("name", t.getName()));
        }
        return true;
    }
    private boolean rtp(Player p) {
        World w = p.getWorld();
        WorldBorder wb = w.getWorldBorder();
        BoundingBox box = BoundingBox.of(wb.getCenter(), wb.getSize() / 2.0, 256, wb.getSize() / 2.0);
        for (int i = 0; i < MAX_TRIES; i++) {
            int r = ThreadLocalRandom.current().nextInt(MIN_RADIUS, MAX_RADIUS + 1);
            double theta = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
            int x = (int) Math.round(p.getLocation().getX() + r * Math.cos(theta));
            int z = (int) Math.round(p.getLocation().getZ() + r * Math.sin(theta));
            if (!box.contains(x, p.getY(), z)) continue;
            int y = w.getHighestBlockYAt(x, z);
            if (y <= w.getMinHeight()) continue;
            Location dest = new Location(w, x + 0.5, y + 1, z + 0.5);
            if (!isSafe(dest)) continue;
            p.teleport(dest);
            w.spawnParticle(Particle.CLOUD, dest, 20, 0.6, 0.6, 0.6, 0.02);
            w.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            return true;
        }
        return false;
    }
    private boolean isSafe(Location loc) {
        if (!loc.getBlock().isEmpty()) return false;
        if (!loc.clone().add(0, 1, 0).getBlock().isEmpty()) return false;
        if (loc.clone().add(0, -1, 0).getBlock().isLiquid()) return false;
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission(PERM_OTHERS)) {
            Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
        }
        return out;
    }
}
