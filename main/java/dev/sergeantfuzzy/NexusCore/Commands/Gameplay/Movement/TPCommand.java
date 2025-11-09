package dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Movement;

import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public final class TPCommand implements TabExecutor {
    private static final String PERM_TP_SELF   = "nexuscore.tp";
    private static final String PERM_TP_HERE   = "nexuscore.tphere";
    private static final String PERM_TP_OTHER  = "nexuscore.tp.other";
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final JavaPlugin plugin;
    public TPCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args
    ) {
        final String lowerLabel = label.toLowerCase(Locale.ROOT);
        try {
            if (lowerLabel.equals("tphere")) {
                return handleTpHere(sender, args);
            }
            return handleTp(sender, args);
        } catch (Exception ex) {
            plugin.getLogger().warning("TPCommand error: " + ex.getMessage());
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Something went wrong executing that command.</red>");
            return true;
        }
    }
    private boolean handleTpHere(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Console cannot pull players. Use </red><yellow>/tp <a> <b></yellow><red> instead.</red>");
            return true;
        }
        if (!sender.hasPermission(PERM_TP_HERE)) {
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>You don't have permission.</red>");
            return true;
        }
        if (args.length < 1) {
            usageTphere(sender);
            return true;
        }
        Player target = requireOnline(args[0], sender);
        if (target == null) return true;
        boolean ok = target.teleport(player.getLocation());
        if (ok) {
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<gray>Teleported </gray><green>" + target.getName() + "</green><gray> to you.</gray>");
            if (!target.equals(player)) {
                i18n.sendMM(target, Msg.CHAT_PREFIX_MM + "<gray>You were teleported to </gray><green>" + player.getName() + "</green><gray>.</gray>");
            }
        } else {
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Teleport failed.</red>");
        }
        return true;
    }
    private boolean handleTp(CommandSender sender, String[] args) {
        final boolean senderIsPlayer = sender instanceof Player;
        if (args.length == 0) {
            quickHelp(sender, senderIsPlayer);
            return true;
        }
        if (args.length >= 2 && looksLikeHere(args[0])) {
            if (!senderIsPlayer) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Console cannot pull players. Use </red><yellow>/tp <a> <b></yellow><red>.</red>");
                return true;
            }
            if (!sender.hasPermission(PERM_TP_HERE)) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>You don't have permission.</red>");
                return true;
            }
            Player player = (Player) sender;
            Player target = requireOnline(args[1], sender);
            if (target == null) return true;
            boolean ok = target.teleport(player.getLocation());
            if (ok) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<gray>Teleported </gray><green>" + target.getName() + "</green><gray> to you.</gray>");
                if (!target.equals(player)) {
                    i18n.sendMM(target, Msg.CHAT_PREFIX_MM + "<gray>You were teleported to </gray><green>" + player.getName() + "</green><gray>.</gray>");
                }
            } else {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Teleport failed.</red>");
            }
            return true;
        }
        if (args.length == 1) {
            if (!senderIsPlayer) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Console usage:</red> <yellow>/tp <playerA> <playerB></yellow>");
                return true;
            }
            if (!sender.hasPermission(PERM_TP_SELF)) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>You don't have permission.</red>");
                return true;
            }
            Player to = requireOnline(args[0], sender);
            if (to == null) return true;
            Player p = (Player) sender;
            if (p.equals(to)) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<yellow>You are already at your own location.</yellow>");
                return true;
            }
            boolean ok = p.teleport(to.getLocation());
            if (ok) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<gray>Teleported to </gray><green>" + to.getName() + "</green><gray>.</gray>");
            } else {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Teleport failed.</red>");
            }
            return true;
        }
        if (!sender.hasPermission(PERM_TP_OTHER)) {
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>You don't have permission.</red>");
            return true;
        }
        Player a = requireOnline(args[0], sender);
        Player b = requireOnline(args[1], sender);
        if (a == null || b == null) return true;
        if (a.equals(b)) {
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<yellow>Those are the same player.</yellow>");
            return true;
        }
        boolean ok = a.teleport(b.getLocation());
        if (ok) {
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<gray>Teleported </gray><green>" + a.getName() + "</green><gray> to </gray><green>" + b.getName() + "</green><gray>.</gray>");
            if (!sender.equals(a)) {
                i18n.sendMM(a, Msg.CHAT_PREFIX_MM + "<gray>You were teleported to </gray><green>" + b.getName() + "</green><gray>.</gray>");
            }
        } else {
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Teleport failed.</red>");
        }
        return true;
    }
    private void usageTphere(CommandSender sender) {
        i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<gray>Usage:</gray> <yellow>/tphere <player></yellow>");
    }
    private void quickHelp(CommandSender sender, boolean senderIsPlayer) {
        List<String> lines = new ArrayList<>();
        lines.add("<gray>Usage:</gray>");
        if (senderIsPlayer) {
            lines.add("<yellow>/tp <player></yellow> <dark_gray>—</dark_gray> <gray>Teleport to player</gray>");
            lines.add("<yellow>/tphere <player></yellow> <dark_gray>—</dark_gray> <gray>Teleport player to you</gray>");
            lines.add("<yellow>/tp <playerA> <playerB></yellow> <dark_gray>—</dark_gray> <gray>Teleport playerA to playerB</gray>");
        } else {
            lines.add("<yellow>/tp <playerA> <playerB></yellow> <dark_gray>—</dark_gray> <gray>Teleport playerA to playerB</gray>");
        }
        i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + String.join("\n", lines));
    }
    @Nullable
    private Player requireOnline(String name, CommandSender feedback) {
        Player exact = Bukkit.getPlayerExact(name);
        if (exact != null && exact.isOnline()) return exact;
        List<Player> partial = Bukkit.matchPlayer(name);
        if (!partial.isEmpty()) return partial.get(0);
        List<String> online = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName).toList();
        String suggestion = closestByEditDistance(name, online, 2);
        if (suggestion != null) {
            i18n.sendMM(feedback, Msg.CHAT_PREFIX_MM +
                    "<red>Player not found:</red> <yellow>" + name + "</yellow> <gray>— did you mean </gray><green>" + suggestion + "</green><gray>?</gray>");
        } else {
            i18n.sendMM(feedback, Msg.CHAT_PREFIX_MM + "<red>Player not found or offline:</red> <yellow>" + name + "</yellow>");
        }
        return null;
    }
    private boolean looksLikeHere(String token) {
        String s = token.toLowerCase(Locale.ROOT);
        if (s.equals("here") || s.equals("tphere")) return true;
        return editDistance(s, "here") <= 2 || editDistance(s, "tphere") <= 2;
    }
    @Nullable
    private String closestByEditDistance(String input, Collection<String> candidates, int maxDistance) {
        String best = null;
        int bestD = Integer.MAX_VALUE;
        for (String c : candidates) {
            int d = editDistance(input.toLowerCase(Locale.ROOT), c.toLowerCase(Locale.ROOT));
            if (d < bestD) {
                bestD = d;
                best = c;
            }
        }
        return bestD <= maxDistance ? best : null;
    }
    private int editDistance(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] cur  = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                cur[j] = Math.min(
                        Math.min(cur[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost
                );
            }
            int[] tmp = prev; prev = cur; cur = tmp;
        }
        return prev[b.length()];
    }
    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args
    ) {
        final String lower = alias.toLowerCase(Locale.ROOT);
        List<String> names = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        if (lower.equals("tphere")) {
            if (args.length == 1) {
                return prefixFilter(names, args[0]);
            }
            return Collections.emptyList();
        }
        if (args.length == 1) {
            List<String> base = new ArrayList<>(names);
            base.add("here");
            return prefixFilter(base, args[0]);
        }
        if (args.length == 2) {
            return prefixFilter(names, args[1]);
        }
        return Collections.emptyList();
    }
    private List<String> prefixFilter(List<String> items, String prefix) {
        String p = prefix.toLowerCase(Locale.ROOT);
        return items.stream()
                .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(p))
                .limit(20)
                .collect(Collectors.toList());
    }
}
