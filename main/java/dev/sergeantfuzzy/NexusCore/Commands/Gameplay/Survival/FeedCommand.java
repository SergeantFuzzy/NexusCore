package dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Survival;

import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Util.GameplayCommandUtil.*;

public final class FeedCommand implements CommandExecutor, TabCompleter {
    private static final String PERM_SELF   = "nexuscore.feed";
    private static final String PERM_OTHERS = "nexuscore.feed.others";
    public FeedCommand(JavaPlugin plugin) {
        // (reserved for future: config hooks, cooldowns, etc.)
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("status")) {
            if (args.length == 1) {
                if (!sender.hasPermission(PERM_SELF)) {
                    i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>You lack permission.</red>");
                    return true;
                }
                if (!ensurePlayerSender(sender)) return true;
                Player p = (Player) sender;
                sendStatus(sender, p);
                return true;
            }
            if (!sender.hasPermission(PERM_OTHERS)) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Only admins may view others.</red>");
                return true;
            }
            Optional<Player> opt = findPlayer(args[1]);
            if (opt.isEmpty()) {
                sendPrefixedMM(sender, "<red>Player <b><name></b> is not online.</red>",
                        Placeholder.unparsed("name", args[1]));
                return true;
            }
            sendStatus(sender, opt.get());
            return true;
        }
        if (args.length == 0) {
            if (!sender.hasPermission(PERM_SELF)) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>You lack permission.</red>");
                return true;
            }
            if (!ensurePlayerSender(sender)) return true;
            Player p = (Player) sender;
            feed(p);
            sendPrefixedMM(sender,
                    "<green>Fed yourself.</green> " + choiceBar("/feed", "Feed yourself", "Check feed status", "Feed others"));
            return true;
        }
        if (!sender.hasPermission(PERM_OTHERS)) {
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Only admins may feed others.</red>");
            return true;
        }
        Optional<Player> opt = findPlayer(args[0]);
        if (opt.isEmpty()) {
            sendPrefixedMM(sender, "<red>Player <b><name></b> is not online.</red>",
                    Placeholder.unparsed("name", args[0]));
            return true;
        }
        Player target = opt.get();
        feed(target);
        sendPrefixedMM(sender, "<green>Fed <b><name></b>.</green>",
                Placeholder.unparsed("name", target.getName()));
        sendPrefixedMM(target, "<green>You were fed by <b><name></b>.</green>",
                Placeholder.unparsed("name", sender.getName()));
        return true;
    }
    private void feed(Player p) {
        p.setFoodLevel(20);
        p.setSaturation(20f);
        p.setExhaustion(0f);
    }
    private void sendStatus(CommandSender viewer, Player target) {
        sendPrefixedMM(viewer,
                "<gray>Feed status for <b><name></b>:</gray> <green>Food:</green> <b><food></b>/<max> <gray>|</gray> <green>Saturation:</green> <b><sat></b> " +
                        choiceBar("/feed", "Feed yourself", "Check feed status", "Feed others"),
                Placeholder.unparsed("name", target.getName()),
                Placeholder.unparsed("food", String.valueOf(target.getFoodLevel())),
                Placeholder.unparsed("max", "20"),
                Placeholder.unparsed("sat", String.format("%.1f", target.getSaturation())));
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
