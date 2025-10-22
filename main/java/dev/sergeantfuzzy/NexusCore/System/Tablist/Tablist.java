package dev.sergeantfuzzy.NexusCore.System.Tablist;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class Tablist implements Listener {
    private static JavaPlugin plugin;
    private static boolean registered = false;
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String CFG_PATH = "Systems/Tablist.yml";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static volatile String titleMM;
    private static volatile ZoneId timeZone;
    private static final List<String> headerLines = new ArrayList<>();
    private static final List<String> footerLines = new ArrayList<>();
    private static volatile boolean nameFmtEnabled = false;
    private static volatile String nameFmt = "<white>{player}</white>";
    static { bootstrap(); }
    private static void bootstrap() {
        if (registered) return;
        try {
            plugin = JavaPlugin.getProvidingPlugin(Tablist.class);
        } catch (Throwable ignore) {
            Plugin p = Bukkit.getPluginManager().getPlugin("NexusCore");
            if (p instanceof JavaPlugin jp) plugin = jp;
        }
        if (plugin == null) return;
        saveDefaultResource();
        reloadLocalConfig();
        Bukkit.getPluginManager().registerEvents(new Tablist(), plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isEnabled()) { clearAll(); return; }
            refreshAll();
        }, 40L, 40L);
        registered = true;
    }
    public static void reloadAndApply() {
        saveDefaultResource();
        reloadLocalConfig();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!isEnabled()) { clearAll(); return; }
            for (Player p : Bukkit.getOnlinePlayers()) {
                applyTo(p);
            }
        });
    }
    private static boolean isEnabled() {
        return plugin != null && plugin.getConfig().getBoolean("System.Tablist", true);
    }
    private static void saveDefaultResource() {
        try {
            File file = new File(plugin.getDataFolder(), CFG_PATH);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                plugin.saveResource(CFG_PATH, false);
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("[Tablist] Could not save default Systems/Tablist.yml: " + t.getMessage());
        }
    }
    public static void reloadLocalConfig() {
        try {
            File file = new File(plugin.getDataFolder(), CFG_PATH);
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            titleMM = yml.getString("Title",
                    "<gradient:#3498DB:#9B59B6><bold>NexusCore</bold></gradient> <gray>»</gray> <italic>Network</italic>");
            String tzRaw = yml.getString("Timezone", "America/Detroit");
            timeZone = resolveZoneId(tzRaw);
            headerLines.clear();
            footerLines.clear();
            List<String> hdr = yml.getStringList("Header");
            List<String> ftr = yml.getStringList("Footer");
            if (hdr == null || hdr.isEmpty()) {
                headerLines.addAll(List.of(
                        "<gray>┌──────────────────────────────┐</gray>",
                        "<white>Welcome to</white> <gradient:#3498DB:#9B59B6><bold>{server}</bold></gradient>",
                        "<gray>Players:</gray> <green>{online}</green><gray>/</gray><white>{max}</white>",
                        "<gray>└──────────────────────────────┘</gray>"
                ));
            } else headerLines.addAll(hdr);
            if (ftr == null || ftr.isEmpty()) {
                footerLines.addAll(List.of(
                        "<gray>TPS:</gray> <gold>{tps}</gold> <dark_gray>•</dark_gray> <gray>Your Ping:</gray> <gold>{ping}</gold>",
                        "<gray>Time:</gray> <white>{time}</white> <dark_gray>({timezone})</dark_gray>"
                ));
            } else footerLines.addAll(ftr);
            nameFmtEnabled = yml.getBoolean("PlayerName.Enabled", false);
            nameFmt = yml.getString("PlayerName.Format", "<white>{player}</white>");
        } catch (Exception ex) {
            plugin.getLogger().warning("[Tablist] Failed to load Systems/Tablist.yml: " + ex.getMessage());
            // Minimal safe defaults
            titleMM = "<gradient:#3498DB:#9B59B6><bold>NexusCore</bold></gradient>";
            timeZone = resolveZoneId("America/Detroit");
            headerLines.clear();
            headerLines.addAll(List.of(
                    "<white>Welcome to</white> <gradient:#3498DB:#9B59B6><bold>{server}</bold></gradient>",
                    "<gray>Players:</gray> <green>{online}</green><gray>/</gray><white>{max}</white>"
            ));
            footerLines.clear();
            footerLines.addAll(List.of(
                    "<gray>TPS:</gray> <gold>{tps}</gold> <dark_gray>•</dark_gray> <gray>Your Ping:</gray> <gold>{ping}</gold>"
            ));
            nameFmtEnabled = false;
            nameFmt = "<white>{player}</white>";
        }
    }
    private static ZoneId resolveZoneId(String raw) {
        if (raw == null || raw.isBlank()) return ZoneId.systemDefault();
        String up = raw.trim().toUpperCase(Locale.ROOT);
        Map<String,String> map = Map.ofEntries(
                Map.entry("EST","America/New_York"), Map.entry("EDT","America/New_York"),
                Map.entry("CST","America/Chicago"),  Map.entry("CDT","America/Chicago"),
                Map.entry("MST","America/Denver"),   Map.entry("MDT","America/Denver"),
                Map.entry("PST","America/Los_Angeles"), Map.entry("PDT","America/Los_Angeles"),
                Map.entry("UTC","UTC"), Map.entry("GMT","UTC")
        );
        String remap = map.getOrDefault(up, raw);
        try { return ZoneId.of(remap); } catch (Exception ignored) { return ZoneId.systemDefault(); }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!isEnabled()) return;
        applyTo(e.getPlayer());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
    }
    private static void clearAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            applyHeaderFooterCompat(p, Component.empty(), Component.empty(), "", "");
            try { p.playerListName(null); } catch (Throwable ignored) {}
        }
    }
    private static void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            applyTo(p);
        }
    }
    private static void applyTo(Player p) {
        if (p == null || !p.isOnline()) return;
        String serverName = Bukkit.getServer().getName();
        String motd = Optional.ofNullable(Bukkit.getMotd()).orElse(serverName);
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        String tpsStr = "—";
        try {
            double[] tpsArr = Bukkit.getServer().getTPS();
            if (tpsArr != null && tpsArr.length > 0) {
                tpsStr = String.format(Locale.US, "%.1f", Math.min(20.0, tpsArr[0]));
            }
        } catch (Throwable ignored) {}
        String pingStr = "—";
        try { pingStr = String.valueOf(p.getPing()); } catch (Throwable ignored) {}
        String timeStr = ZonedDateTime.now(timeZone).format(TIME_FMT);
        String headerMMJoined = joinLines(replaceAll(headerLines, p, serverName, motd, online, max, tpsStr, pingStr, timeStr));
        String footerMMJoined = joinLines(replaceAll(footerLines, p, serverName, motd, online, max, tpsStr, pingStr, timeStr));
        Component header = MM.deserialize(headerMMJoined);
        Component footer = MM.deserialize(footerMMJoined);
        String headerPlain = stripMiniMessage(headerMMJoined);
        String footerPlain = stripMiniMessage(footerMMJoined);
        applyHeaderFooterCompat(p, header, footer, headerPlain, footerPlain);
        if (nameFmtEnabled) {
            String nm = nameFmt.replace("{player}", p.getName());
            try {
                p.playerListName(MM.deserialize(nm));
            } catch (Throwable notPaper) {
                try {
                    Player.class.getMethod("setPlayerListName", String.class)
                            .invoke(p, stripMiniMessage(nm));
                } catch (Throwable ignored) { /* no-op */ }
            }
        }
    }
    private static String joinLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) return "";
        return String.join("\n", lines);
    }
    private static List<String> replaceAll(
            List<String> src, Player p, String serverName, String motd,
            int online, int max, String tpsStr, String pingStr, String timeStr
    ) {
        List<String> out = new ArrayList<>(src.size());
        for (String s : src) {
            if (s == null) s = "";
            s = s.replace("{title}", titleMM) // <-- add this line
                    .replace("{server}", serverName)
                    .replace("{motd}", motd)
                    .replace("{online}", String.valueOf(online))
                    .replace("{max}", String.valueOf(max))
                    .replace("{tps}", tpsStr)
                    .replace("{ping}", pingStr)
                    .replace("{time}", timeStr)
                    .replace("{timezone}", timeZone.getId())
                    .replace("{player}", p.getName());
            out.add(s);
        }
        return out;
    }
    private static void applyHeaderFooterCompat(Player p, Component header, Component footer, String headerPlain, String footerPlain) {
        try {
            p.sendPlayerListHeaderAndFooter(header, footer);
            return;
        } catch (Throwable ignored) { /* not Paper or too old */ }
        try {
            Player.class.getMethod("setPlayerListHeaderFooter", String.class, String.class)
                    .invoke(p, headerPlain, footerPlain);
            return;
        } catch (Throwable ignored) { /* method not present */ }
        try {
            Object spigot = Player.class.getMethod("spigot").invoke(p);
            Class<?> BaseComponent = Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            Class<?> TextComponent = Class.forName("net.md_5.bungee.api.chat.TextComponent");
            Object h = TextComponent.getConstructor(String.class).newInstance(headerPlain);
            Object f = TextComponent.getConstructor(String.class).newInstance(footerPlain);
            spigot.getClass().getMethod("setPlayerListHeaderFooter", BaseComponent, BaseComponent)
                    .invoke(spigot, h, f);
        } catch (Throwable ignored) {
        }
    }
    private static String stripMiniMessage(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.replaceAll("<[^>]+>", "");
    }
}