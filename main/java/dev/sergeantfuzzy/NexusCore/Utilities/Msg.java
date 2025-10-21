package dev.sergeantfuzzy.NexusCore.Utilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.PrintWriter;
import java.util.List;

public final class Msg {
    private Msg() {}
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String RULE_56 = "────────────────────────────────────────────────────────";
    public static final String CHAT_PREFIX_MM = "<gradient:#3498DB:#9B59B6><bold>NexusCore</bold></gradient> <gray>»</gray> ";
    public static Component prefix() {
        return MiniMessage.miniMessage().deserialize(CHAT_PREFIX_MM);
    }
    public static void sendPrefixed(Player player, Component content) {
        player.sendMessage(Component.text().append(prefix()).append(content));
    }
    public static void sendPrefixed(Player player, String miniMsg) {
        sendPrefixed(player, MiniMessage.miniMessage().deserialize(miniMsg));
    }
    public static Component buildAdminJoinLine(JavaPlugin plugin) {
        final String ver  = plugin.getDescription().getVersion();
        final String type = releaseType(ver);
        final List<String> authors = plugin.getDescription().getAuthors();
        final String authorText = (authors == null || authors.isEmpty()) ? "Unknown" : String.join(", ", authors);

        return MiniMessage.miniMessage().deserialize(
                "<white><b>Version:</b></white> <green><ver></green>  " +
                        "<white><b>Release:</b></white> <gold><type></gold>  " +
                        "<white><b>Author:</b></white> <aqua><author></aqua>",
                Placeholder.unparsed("ver", ver),
                Placeholder.unparsed("type", type),
                Placeholder.unparsed("author", authorText)
        );
    }
    public static String releaseType(String version) {
        if (version == null) return "Full";
        String v = version.toLowerCase();
        if (v.contains("snapshot")) return "Snapshot";
        if (v.contains("beta"))     return "Beta";
        if (v.contains("alpha"))    return "Alpha";
        if (v.contains("rc"))       return "RC";
        return "Full";
    }
    private static final int[] START = {52, 152, 219};
    private static final int[] END   = {155, 89, 182};
    private static final String RESET = "\u001B[0m";
    public static void printBanner(JavaPlugin plugin, String state) {
        final String pluginName = plugin.getDescription().getName();
        final String pluginVer  = plugin.getDescription().getVersion();
        final Platform pf = detectPlatform();
        final String mcVer = safe(Bukkit.getBukkitVersion());
        final String impl  = safe(Bukkit.getVersion());
        final List<String> authors = plugin.getDescription().getAuthors();
        final String authorText = (authors == null || authors.isEmpty()) ? "Unknown" : String.join(", ", authors);
        String line = repeat('═', 56);
        rawGradient(line);
        rawGradient(center(pluginName + " v" + pluginVer, 56));
        rawGradientKV("Author:", authorText, 56);
        rawGradientKV("Status:", state, 56);
        rawGradientKV("Platform:", pf.readable + " • MC " + mcVer, 56);
        rawGradient(center(impl, 56));
        rawGradient(line);
        rawPrint(RESET);
    }
    private static void rawPrint(String text) {
        try {
            PrintWriter console = (System.console() != null) ? System.console().writer() : null;
            if (console != null) {
                console.print(text + "\n");
                console.flush();
            } else {
                System.out.write((text + "\n").getBytes());
                System.out.flush();
            }
        } catch (Exception ignored) {}
    }
    private static void rawGradient(String text) {
        StringBuilder sb = new StringBuilder(text.length() * 12);
        int len = text.length();
        for (int i = 0; i < len; i++) {
            double t = (len == 1) ? 0.0 : (double) i / (len - 1);
            int r = (int) (START[0] + (END[0] - START[0]) * t);
            int g = (int) (START[1] + (END[1] - START[1]) * t);
            int b = (int) (START[2] + (END[2] - START[2]) * t);
            sb.append("\u001B[38;2;").append(r).append(';').append(g).append(';').append(b).append('m')
                    .append(text.charAt(i));
        }
        sb.append(RESET);
        rawPrint(sb.toString());
    }
    private static void rawGradientKV(String field, String value, int width) {
        String combined = field + " " + (value == null ? "" : value);
        String text = center(combined, width);
        int startField = text.indexOf(field);
        int endField   = startField + field.length();
        StringBuilder sb = new StringBuilder(text.length() * 12);
        boolean boldOn = false;
        int len = text.length();
        for (int i = 0; i < len; i++) {
            double t = (len == 1) ? 0.0 : (double) i / (len - 1);
            int r = (int) Math.round(START[0] + (END[0] - START[0]) * t);
            int g = (int) Math.round(START[1] + (END[1] - START[1]) * t);
            int b = (int) Math.round(START[2] + (END[2] - START[2]) * t);
            sb.append("\u001B[38;2;").append(r).append(';').append(g).append(';').append(b).append('m');
            if (i == startField && !boldOn) { sb.append("\u001B[1m"); boldOn = true; }
            if (i == endField && boldOn)     { sb.append("\u001B[22m"); boldOn = false; }
            sb.append(text.charAt(i));
        }
        sb.append(RESET);
        rawPrint(sb.toString());
    }
    private enum Platform {
        SPIGOT("Spigot/CraftBukkit"),
        PAPER("Paper/Purpur"),
        FOLIA("Folia (Regionized)");
        final String readable;
        Platform(String readable) { this.readable = readable; }
    }
    private static Platform detectPlatform() {
        try { Class.forName("io.papermc.paper.threadedregions.RegionizedServer"); return Platform.FOLIA; }
        catch (Throwable ignored) {}
        try { Class.forName("com.destroystokyo.paper.PaperConfig"); return Platform.PAPER; }
        catch (Throwable ignored) {}
        String name = Bukkit.getServer().getName();
        if (name != null && name.toLowerCase().contains("folia")) return Platform.FOLIA;
        if (name != null && name.toLowerCase().contains("paper")) return Platform.PAPER;
        return Platform.SPIGOT;
    }
    private static String safe(String s) { return (s == null) ? "" : s; }
    private static String repeat(char c, int n) { return String.valueOf(c).repeat(n); }
    private static String center(String s, int w) {
        if (s.length() >= w) return s;
        int pad = (w - s.length()) / 2;
        return " ".repeat(pad) + s;
    }
    public static Component consoleRule() {
        return MM.deserialize("<gradient:#3498DB:#9B59B6>" + RULE_56 + "</gradient>");
    }
    public static String consoleRuleMini() {
        return "<gradient:#3498DB:#9B59B6>" + RULE_56 + "</gradient>";
    }
    public static String consoleBrandMini(String name, String version) {
        return "<gradient:#3498DB:#9B59B6><b>" + name + " v" + version + "</b></gradient>";
    }
    public static Component chatRule() {
        return MiniMessage.miniMessage().deserialize(
                "<gradient:#3498DB:#9B59B6>───────────────────────────────</gradient>"
        );
    }
}