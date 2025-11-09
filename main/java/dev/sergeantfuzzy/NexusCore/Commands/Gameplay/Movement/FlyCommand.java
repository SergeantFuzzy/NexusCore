package dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Movement;

import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Util.GameplayCommandUtil.*;

public final class FlyCommand implements CommandExecutor, TabCompleter {
    private static final String PERM_SELF   = "nexuscore.fly";
    private static final String PERM_OTHERS = "nexuscore.fly.others";
    public FlyCommand(JavaPlugin plugin) {
        // (reserved for future: config hooks, cooldowns, etc.)
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("status")) {
            if (args.length == 1) {
                if (!sender.hasPermission(PERM_SELF) || !ensurePlayerSender(sender)) return true;
                Player p = (Player) sender;
                sendStatus(sender, p);
                return true;
            }
            if (!sender.hasPermission(PERM_OTHERS)) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Only admins may check others.</red>");
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
            if (!sender.hasPermission(PERM_SELF) || !ensurePlayerSender(sender)) return true;
            Player p = (Player) sender;
            boolean next = !p.getAllowFlight();
            setFly(p, next);
            sendPrefixedMM(sender, "<green>Flight <b><state></b> for yourself.</green>" +
                            actionBar(
                                    runAction("Toggle", "/fly", "Toggle your flight again"),
                                    runAction("Status", "/fly status", "Check fly status"),
                                    suggestAction("Toggle Player", "/fly ", "Type a player to toggle")
                            ),
                    Placeholder.unparsed("state", next ? "enabled" : "disabled"));
            return true;
        }
        if (args.length == 1) {
            String a = args[0].toLowerCase();
            if ("on".equals(a) || "off".equals(a)) {
                if (!sender.hasPermission(PERM_SELF) || !ensurePlayerSender(sender)) return true;
                Player p = (Player) sender;
                boolean enable = "on".equals(a);
                setFly(p, enable);
                sendPrefixedMM(sender, "<green>Flight <b><state></b> for yourself.</green>" +
                                actionBar(
                                        runAction("Toggle", "/fly", "Toggle flight again"),
                                        runAction("Status", "/fly status", "Check fly status"),
                                        suggestAction("Toggle Player", "/fly ", "Type a player to toggle")
                                ),
                        Placeholder.unparsed("state", enable ? "enabled" : "disabled"));
                return true;
            }
            if (!sender.hasPermission(PERM_OTHERS)) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Only admins may toggle others.</red>");
                return true;
            }
            Optional<Player> opt = findPlayer(args[0]);
            if (opt.isEmpty()) {
                sendPrefixedMM(sender, "<red>Player <b><name></b> is not online.</red>",
                        Placeholder.unparsed("name", args[0]));
                return true;
            }
            Player t = opt.get();
            boolean next = !t.getAllowFlight();
            setFly(t, next);
            sendPrefixedMM(sender, "<green>Flight <b><state></b> for <b><name></b>.</green>" + actionBar(
                            runAction("Toggle Again", "/fly " + t.getName(), "Flip their flight again"),
                            runAction("Status", "/fly status " + t.getName(), "Check their status"),
                            suggestAction("Toggle Player", "/fly ", "Type a different player")
                    ),
                    Placeholder.unparsed("state", next ? "enabled" : "disabled"),
                    Placeholder.unparsed("name", t.getName()));
            sendPrefixedMM(t, "<green>Your flight was <b><state></b> by <b><name></b>.</green>" + actionBar(
                            runAction("Toggle", "/fly", "Toggle your own flight"),
                            runAction("Back", "/back", "Return to your previous spot")
                    ),
                    Placeholder.unparsed("state", next ? "enabled" : "disabled"),
                    Placeholder.unparsed("name", sender.getName()));
            return true;
        }
        if (args.length >= 2) {
            if (!sender.hasPermission(PERM_OTHERS)) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Only admins may set others.</red>");
                return true;
            }
            boolean enable = "on".equalsIgnoreCase(args[0]);
            if (!enable && !"off".equalsIgnoreCase(args[0])) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Usage:</red> /fly [on|off] [player]");
                return true;
            }
            Optional<Player> opt = findPlayer(args[1]);
            if (opt.isEmpty()) {
                sendPrefixedMM(sender, "<red>Player <b><name></b> is not online.</red>",
                        Placeholder.unparsed("name", args[1]));
                return true;
            }
            Player t = opt.get();
            setFly(t, enable);
            sendPrefixedMM(sender, "<green>Flight <b><state></b> for <b><name></b>.</green>" + actionBar(
                            runAction("Toggle Again", "/fly " + t.getName(), "Flip their flight again"),
                            runAction("Status", "/fly status " + t.getName(), "Check their status"),
                            suggestAction("Toggle Player", "/fly ", "Type a different player")
                    ),
                    Placeholder.unparsed("state", enable ? "enabled" : "disabled"),
                    Placeholder.unparsed("name", t.getName()));
            sendPrefixedMM(t, "<green>Your flight was <b><state></b> by <b><name></b>.</green>" + actionBar(
                            runAction("Toggle", "/fly", "Toggle your own flight"),
                            runAction("Back", "/back", "Return to your previous spot")
                    ),
                    Placeholder.unparsed("state", enable ? "enabled" : "disabled"),
                    Placeholder.unparsed("name", sender.getName()));
            return true;
        }
        return true;
    }
    private void setFly(Player p, boolean enable) {
        p.setAllowFlight(enable);
        if (!enable && p.isFlying()) p.setFlying(false);
    }
    private void sendStatus(CommandSender viewer, Player target) {
        sendPrefixedMM(viewer,
                "<gray>Fly status for <b><name></b>:</gray> <green><state></green>" +
                        actionBar(
                                runAction("Toggle " + target.getName(), "/fly " + target.getName(), "Toggle their flight"),
                                runAction("Status", "/fly status " + target.getName(), "Refresh their status"),
                                suggestAction("Toggle Player", "/fly ", "Type another player name")
                        ),
                Placeholder.unparsed("name", target.getName()),
                Placeholder.unparsed("state", target.getAllowFlight() ? "ENABLED" : "DISABLED"));
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.add("status");
            out.add("on");
            out.add("off");
            if (sender.hasPermission(PERM_OTHERS)) {
                Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
            }
        } else if (args.length == 2) {
            if (sender.hasPermission(PERM_OTHERS)) {
                Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
            }
        }
        return out;
    }
}
