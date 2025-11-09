package dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Util;

import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class GameplayCommandUtil {
    private GameplayCommandUtil() {}
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
    private static final String THEME_GRADIENT = "<gradient:#3498DB:#9B59B6>";
    private static final String SEPARATOR = " <dark_gray>•</dark_gray> ";
    public enum ClickType { RUN, SUGGEST }
    public record CommandAction(String label, String command, ClickType click, String hover) {}
    public static CommandAction runAction(String label, String command, String hover) {
        return new CommandAction(label, command, ClickType.RUN, hover);
    }
    public static CommandAction suggestAction(String label, String command, String hover) {
        return new CommandAction(label, command, ClickType.SUGGEST, hover);
    }
    public static String actionBar(CommandAction... actions) {
        if (actions == null || actions.length == 0) return "";
        List<String> parts = new ArrayList<>();
        for (CommandAction action : actions) {
            String rendered = renderAction(action);
            if (!rendered.isEmpty()) parts.add(rendered);
        }
        if (parts.isEmpty()) return "";
        return " <dark_gray>|</dark_gray> " + String.join(SEPARATOR, parts);
    }
    private static String renderAction(CommandAction action) {
        if (action == null) return "";
        String command = sanitize(action.command());
        if (command.isEmpty()) return "";
        String label = sanitize(action.label());
        if (label.isEmpty()) label = "Run";
        ClickType click = (action.click() == null) ? ClickType.RUN : action.click();
        String hover = sanitizeHover(action.hover(), click);
        String clickType = click == ClickType.SUGGEST ? "suggest_command" : "run_command";
        return "<click:" + clickType + ":'" + command + "'>"
                + "<hover:show_text:'" + hover + "'>"
                + THEME_GRADIENT + "<bold>[" + label + "]</bold></gradient>"
                + "</hover></click>";
    }
    private static String sanitize(String in) {
        if (in == null) return "";
        return in.replace("'", "").trim();
    }
    private static String sanitizeHover(String hover, ClickType clickType) {
        String fallback = switch (clickType) {
            case SUGGEST -> "<gray>Click to pre-fill this command.</gray>";
            case RUN -> "<gray>Click to run this command.</gray>";
        };
        String raw = (hover == null || hover.isEmpty()) ? fallback : hover;
        String cleaned = raw.replace("'", "");
        if (cleaned.startsWith("<")) return cleaned;
        return "<gray>" + cleaned + "</gray>";
    }
    /**
     * @deprecated Retained for older call-sites. Prefer {@link #actionBar(CommandAction...)}.
     */
    @Deprecated(forRemoval = true)
    public static String choiceBar(String runCommand, String... hints) {
        if (runCommand == null || runCommand.isEmpty()) return "";
        String hover = (hints != null && hints.length > 0 && hints[0] != null && !hints[0].isEmpty())
                ? hints[0]
                : "Click to run";
        return actionBar(runAction("Run", runCommand, "<green>" + hover + "</green>"));
    }
}
