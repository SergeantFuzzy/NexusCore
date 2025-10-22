package dev.sergeantfuzzy.NexusCore.System.Scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class Scoreboard implements Listener {
    private static JavaPlugin plugin;
    private static boolean registered = false;
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Map<UUID, org.bukkit.scoreboard.Scoreboard> boards = new HashMap<>();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static volatile String titleMM;
    private static volatile String sepTopMM;
    private static volatile String sepBottomMM;
    private static volatile ZoneId timeZone;
    private static final Map<String, LineTemplate> lineTemplates = new LinkedHashMap<>();
    private static final String CFG_PATH = "Systems/Scoreboard.yml";
    static { bootstrap(); }
    private static void bootstrap() {
        if (registered) return;
        try {
            plugin = JavaPlugin.getProvidingPlugin(Scoreboard.class);
        } catch (Throwable ignore) {
            Plugin p = Bukkit.getPluginManager().getPlugin("NexusCore");
            if (p instanceof JavaPlugin jp) plugin = jp;
        }
        if (plugin == null) return;
        saveDefaultResource();
        reloadLocalConfig();
        Bukkit.getPluginManager().registerEvents(new Scoreboard(), plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isEnabled()) { clearAll(); return; }
            refreshAll();
        }, 40L, 40L);
        registered = true;
    }
    public static void reloadLocalConfig() {
        try {
            File file = new File(plugin.getDataFolder(), CFG_PATH);
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            titleMM = yml.getString("Title",
                    "<gradient:#3498DB:#9B59B6><bold>NexusCore Board</bold></gradient>");
            sepTopMM = yml.getString("Separator.Top", "<gray>┌────────────────────┐</gray>");
            sepBottomMM = yml.getString("Separator.Bottom", "<gray>└────────────────────┘</gray>");
            String tzRaw = yml.getString("Timezone", "America/Detroit");
            timeZone = resolveZoneId(tzRaw);
            lineTemplates.clear();
            ConfigurationSection linesSec = yml.getConfigurationSection("Lines");
            if (linesSec != null) {
                for (String key : new TreeSet<>(linesSec.getKeys(false))) {
                    String template = linesSec.getString(key + ".template", "");
                    List<String> req = linesSec.getStringList(key + ".required_placeholders");
                    lineTemplates.put(key, new LineTemplate(key, template, req));
                }
            }
            if (lineTemplates.isEmpty()) loadBuiltInDefaults();
        } catch (Exception ex) {
            plugin.getLogger().warning("[Scoreboard] Failed to load Systems/Scoreboard.yml: " + ex.getMessage());
            loadBuiltInDefaults();
            timeZone = resolveZoneId("America/Detroit");
        }
    }
    public static void reloadAndApply() {
        saveDefaultResource();
        reloadLocalConfig();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!isEnabled()) {
                clearAll();
                return;
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                setup(p);
            }
        });
    }
    private static void saveDefaultResource() {
        try {
            File file = new File(plugin.getDataFolder(), CFG_PATH);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                plugin.saveResource(CFG_PATH, false);
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("[Scoreboard] Could not save default Systems/Scoreboard.yml: " + t.getMessage());
        }
    }
    private static void loadBuiltInDefaults() {
        titleMM = "<gradient:#3498DB:#9B59B6><bold>NexusCore Board</bold></gradient>";
        sepTopMM = "<gray>┌────────────────────┐</gray>";
        sepBottomMM = "<gray>└────────────────────┘</gray>";
        lineTemplates.clear();
        lineTemplates.put("L01_TopRule",    new LineTemplate("L01_TopRule",    sepTopMM, List.of()));
        lineTemplates.put("L02_Player",     new LineTemplate("L02_Player",     "<white>Player:</white> <green>{player}</green>", List.of("{player}")));
        lineTemplates.put("L03_Rank",       new LineTemplate("L03_Rank",       "<white>Rank:</white> <aqua>{rank}</aqua>",        List.of("{rank}")));
        lineTemplates.put("L04_World",      new LineTemplate("L04_World",      "<white>World:</white> <yellow>{world}</yellow>",  List.of("{world}")));
        lineTemplates.put("L05_XYZ",        new LineTemplate("L05_XYZ",        "<white>XYZ:</white> <gray>{x}</gray><dark_gray>,</dark_gray><gray>{y}</gray><dark_gray>,</dark_gray><gray>{z}</gray>", List.of("{x}","{y}","{z}")));
        lineTemplates.put("L06_Online",     new LineTemplate("L06_Online",     "<white>Online:</white> <green>{online}</green>",  List.of("{online}")));
        lineTemplates.put("L07_Tps",        new LineTemplate("L07_Tps",        "<white>TPS:</white> <gold>{tps}</gold>",          List.of("{tps}")));
        lineTemplates.put("L08_Ping",       new LineTemplate("L08_Ping",       "<white>Ping:</white> <gold>{ping}</gold>",        List.of("{ping}")));
        lineTemplates.put("L09_Spacer1",    new LineTemplate("L09_Spacer1",    "<gray> </gray>",                                   List.of()));
        lineTemplates.put("L10_Tip",        new LineTemplate("L10_Tip",        "<gray>Tips:</gray> <dark_gray>/nexus</dark_gray>",List.of()));
        lineTemplates.put("L11_Discord",    new LineTemplate("L11_Discord",    "<gray>Discord:</gray> <blue>{discord}</blue>",    List.of("{discord}")));
        lineTemplates.put("L12_Website",    new LineTemplate("L12_Website",    "<gray>Website:</gray> <blue>{website}</blue>",    List.of("{website}")));
        lineTemplates.put("L13_Spacer2",    new LineTemplate("L13_Spacer2",    "<gray> </gray>",                                   List.of()));
        lineTemplates.put("L14_Time",       new LineTemplate("L14_Time",       "<gray>Time:</gray> <white>{time}</white>",        List.of("{time}")));
        lineTemplates.put("L15_BottomRule", new LineTemplate("L15_BottomRule", sepBottomMM,                                        List.of()));
    }
    private static ZoneId resolveZoneId(String raw) {
        if (raw == null || raw.isBlank()) return ZoneId.systemDefault();
        String up = raw.trim().toUpperCase(Locale.ROOT);
        Map<String, String> map = Map.ofEntries(
                Map.entry("EST", "America/New_York"),
                Map.entry("EDT", "America/New_York"),
                Map.entry("CST", "America/Chicago"),
                Map.entry("CDT", "America/Chicago"),
                Map.entry("MST", "America/Denver"),
                Map.entry("MDT", "America/Denver"),
                Map.entry("PST", "America/Los_Angeles"),
                Map.entry("PDT", "America/Los_Angeles"),
                Map.entry("UTC", "UTC"),
                Map.entry("GMT", "UTC")
        );
        String remap = map.getOrDefault(up, raw);
        try {
            return ZoneId.of(remap);
        } catch (Exception ignored) {
            return ZoneId.systemDefault();
        }
    }
    private static boolean isEnabled() {
        return plugin != null && plugin.getConfig().getBoolean("System.Scoreboard", true);
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!isEnabled()) return;
        setup(e.getPlayer());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        boards.remove(e.getPlayer().getUniqueId());
    }
    private static void clearAll() {
        var mgr = Bukkit.getScoreboardManager();
        if (mgr == null) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(mgr.getMainScoreboard());
        }
        boards.clear();
    }
    private static void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!boards.containsKey(p.getUniqueId())) setup(p);
            else updateLines(p);
        }
    }
    private static void setup(Player p) {
        var mgr = Bukkit.getScoreboardManager();
        if (mgr == null) return;
        org.bukkit.scoreboard.Scoreboard sb = mgr.getNewScoreboard();
        Objective obj = sb.registerNewObjective("nexuscore", "dummy", MM.deserialize(titleMM));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        String[] entries = new String[] {
                ChatColor.BLACK.toString(), ChatColor.DARK_BLUE.toString(),
                ChatColor.DARK_GREEN.toString(), ChatColor.DARK_AQUA.toString(),
                ChatColor.DARK_RED.toString(), ChatColor.DARK_PURPLE.toString(),
                ChatColor.GOLD.toString(), ChatColor.GRAY.toString(),
                ChatColor.DARK_GRAY.toString(), ChatColor.BLUE.toString(),
                ChatColor.GREEN.toString(), ChatColor.AQUA.toString(),
                ChatColor.RED.toString(), ChatColor.LIGHT_PURPLE.toString(),
                ChatColor.YELLOW.toString()
        };
        for (int i = 0; i < 15; i++) {
            String key = entries[i];
            Team t = getOrCreateTeam(sb, "L" + i, key);
            obj.getScore(key).setScore(15 - i);
        }
        boards.put(p.getUniqueId(), sb);
        p.setScoreboard(sb);
        updateLines(p);
    }
    private static Team getOrCreateTeam(org.bukkit.scoreboard.Scoreboard sb, String name, String entry) {
        Team t = sb.getTeam(name);
        if (t == null) t = sb.registerNewTeam(name);
        if (!t.hasEntry(entry)) t.addEntry(entry);
        return t;
    }
    private static void setLine(org.bukkit.scoreboard.Scoreboard sb, int index, Component comp) {
        Team t = sb.getTeam("L" + index);
        if (t != null) {
            t.prefix(Component.empty());
            t.suffix(comp);
        }
    }
    private static void updateLines(Player p) {
        org.bukkit.scoreboard.Scoreboard sb = boards.get(p.getUniqueId());
        if (sb == null) return;
        int online = Bukkit.getOnlinePlayers().size();
        String world = p.getWorld().getName();
        int x = p.getLocation().getBlockX();
        int y = p.getLocation().getBlockY();
        int z = p.getLocation().getBlockZ();
        String ping = "—";
        try { ping = String.valueOf(p.getPing()); } catch (Throwable ignored) {}
        String tps = "—";
        try {
            double[] tpsArr = Bukkit.getServer().getTPS();
            if (tpsArr != null && tpsArr.length > 0) {
                tps = String.format(Locale.US, "%.1f", Math.min(20.0, tpsArr[0]));
            }
        } catch (Throwable ignored) {}
        String timeStr = ZonedDateTime.now(timeZone).format(TIME_FMT);
        String discord = "discord.gg/yourlink";
        String website = "sergeantfuzzy.dev";
        List<Component> comps = new ArrayList<>(15);
        int i = 0;
        for (LineTemplate lt : lineTemplates.values()) {
            String mm = lt.template;
            if (!lt.requiredPlaceholdersSatisfied(mm)) {
                mm = lt.defaultOrFallback();
            }
            mm = mm.replace("{player}", p.getName())
                    .replace("{rank}", p.isOp() ? "Operator" : "Member")
                    .replace("{world}", world)
                    .replace("{x}", String.valueOf(x))
                    .replace("{y}", String.valueOf(y))
                    .replace("{z}", String.valueOf(z))
                    .replace("{online}", String.valueOf(online))
                    .replace("{tps}", tps)
                    .replace("{ping}", ping)
                    .replace("{time}", timeStr)
                    .replace("{discord}", discord)
                    .replace("{website}", website)
                    .replace("{sep_top}", sepTopMM)
                    .replace("{sep_bottom}", sepBottomMM);
            comps.add(MM.deserialize(mm));
            i++;
        }
        while (comps.size() < 15) comps.add(Component.empty());
        for (int idx = 0; idx < 15; idx++) {
            setLine(sb, idx, comps.get(idx));
        }
    }
    private record LineTemplate(String key, String template, List<String> required) {
        boolean requiredPlaceholdersSatisfied(String s) {
            if (required == null || required.isEmpty()) return true;
            for (String r : required) {
                if (r == null || r.isBlank()) continue;
                if (s == null || !s.contains(r)) return false;
            }
            return true;
        }
        String defaultOrFallback() {
            return switch (key) {
                case "L01_TopRule"    -> "<gray>┌────────────────────┐</gray>";
                case "L15_BottomRule" -> "<gray>└────────────────────┘</gray>";
                case "L02_Player"     -> "<white>Player:</white> <green>{player}</green>";
                case "L03_Rank"       -> "<white>Rank:</white> <aqua>{rank}</aqua>";
                case "L04_World"      -> "<white>World:</white> <yellow>{world}</yellow>";
                case "L05_XYZ"        -> "<white>XYZ:</white> <gray>{x}</gray><dark_gray>,</dark_gray><gray>{y}</gray><dark_gray>,</dark_gray><gray>{z}</gray>";
                case "L06_Online"     -> "<white>Online:</white> <green>{online}</green>";
                case "L07_Tps"        -> "<white>TPS:</white> <gold>{tps}</gold>";
                case "L08_Ping"       -> "<white>Ping:</white> <gold>{ping}</gold>";
                case "L10_Tip"        -> "<gray>Tips:</gray> <dark_gray>/nexus</dark_gray>";
                case "L11_Discord"    -> "<gray>Discord:</gray> <blue>{discord}</blue>";
                case "L12_Website"    -> "<gray>Website:</gray> <blue>{website}</blue>";
                case "L14_Time"       -> "<gray>Time:</gray> <white>{time}</white>";
                case "L09_Spacer1", "L13_Spacer2" -> "<gray> </gray>";
                default -> "<gray> </gray>";
            };
        }
    }
}