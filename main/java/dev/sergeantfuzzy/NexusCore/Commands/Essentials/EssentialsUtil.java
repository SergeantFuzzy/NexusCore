package dev.sergeantfuzzy.NexusCore.Commands.Essentials;

import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;

public final class EssentialsUtil {
    private EssentialsUtil() {}
    public static void sendPrefixedMM(CommandSender sender, String body) {
        i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + body);
    }
    public static void sendPrefixedMM(CommandSender sender, String body, TagResolver... resolvers) {
        i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + body, resolvers);
    }
    public static void sendPrefixedMM(Player player, String body) {
        i18n.sendMM(player, Msg.CHAT_PREFIX_MM + body);
    }
    public static void sendPrefixedMM(Player player, String body, TagResolver... resolvers) {
        i18n.sendMM(player, Msg.CHAT_PREFIX_MM + body, resolvers);
    }
    public static boolean ensurePlayerSender(CommandSender sender) {
        if (sender instanceof Player) return true;
        sendPrefixedMM(sender, "<red>Only players may use this.</red>");
        return false;
    }
    public static Optional<Player> findPlayer(String name) {
        if (name == null || name.isEmpty()) return Optional.empty();
        Player exact = Bukkit.getPlayerExact(name);
        if (exact != null) return Optional.of(exact);
        String q = name.toLowerCase(Locale.ROOT);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(name)) return Optional.of(p);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase(Locale.ROOT).startsWith(q)) return Optional.of(p);
        }
        return Optional.empty();
    }
    public static String choiceBar(String runCommand, String... hints) {
        if (runCommand == null || runCommand.isEmpty()) return "";
        String hover = (hints != null && hints.length > 0 && hints[0] != null && !hints[0].isEmpty())
                ? hints[0]
                : "Click to run";
        hover = hover.replace("'", "");
        return " <dark_gray>|</dark_gray> "
                + "<click:run_command:'" + runCommand + "'>"
                + "<hover:show_text:'<green>" + hover + "</green>'>"
                + "<green>[Click to Run]</green>"
                + "</hover></click>";
    }
}