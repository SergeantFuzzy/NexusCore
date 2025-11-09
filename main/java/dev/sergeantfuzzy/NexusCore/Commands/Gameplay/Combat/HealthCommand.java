package dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Combat;

import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Util.GameplayCommandUtil.*;

public final class HealthCommand implements CommandExecutor, TabCompleter {
    private static final String PERM_SELF   = "nexuscore.health";
    private static final String PERM_OTHERS = "nexuscore.health.others";
    public HealthCommand(JavaPlugin plugin) {
        // (reserved for future: config hooks, cooldowns, etc.)
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean status = args.length >= 1 && args[0].equalsIgnoreCase("status");
        if (args.length == 0 || status && args.length == 1) {
            if (!sender.hasPermission(PERM_SELF)) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>You lack permission.</red>");
                return true;
            }
            if (!ensurePlayerSender(sender)) return true;
            Player p = (Player) sender;
            send(sender, p);
            return true;
        }
        String name = status ? args[1] : args[0];
        boolean checkOthers = sender.hasPermission(PERM_OTHERS);
        if (!checkOthers) {
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Only admins may view others.</red>");
            return true;
        }
        Optional<Player> opt = findPlayer(name);
        if (opt.isEmpty()) {
            sendPrefixedMM(sender, "<red>Player <b><name></b> is not online.</red>",
                    Placeholder.unparsed("name", name));
            return true;
        }
        send(sender, opt.get());
        return true;
    }
    private void send(CommandSender viewer, Player target) {
        double max = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        sendPrefixedMM(viewer,
                "<gray>Health for <b><name></b>:</gray> <green><cur></green>/<max> <gray>HP</gray> " +
                        choiceBar("/health", "Check your health", "Check health status", "Check others"),
                Placeholder.unparsed("name", target.getName()),
                Placeholder.unparsed("cur", String.format("%.1f", target.getHealth())),
                Placeholder.unparsed("max", String.format("%.1f", max)));
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.add("status");
            if (sender.hasPermission(PERM_OTHERS)) {
                Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
            }
        } else if (args.length == 2 && "status".equalsIgnoreCase(args[0]) && sender.hasPermission(PERM_OTHERS)) {
            Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
        }
        return out;
    }
}
