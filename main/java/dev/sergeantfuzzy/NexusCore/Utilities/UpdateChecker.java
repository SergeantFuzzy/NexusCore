package dev.sergeantfuzzy.NexusCore.Utilities;

import dev.sergeantfuzzy.NexusCore.Update.UpdateCheckService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UpdateChecker implements UpdateCheckService {
    private final JavaPlugin plugin;
    private final int spigotResourceId;
    private final String builtByBitUrl;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private static final int RULE_WIDTH = 56;
    private static volatile long lastConsoleNoticeMs = 0L;
    private static final long CONSOLE_DEBOUNCE_MS = 5 * 60 * 1000;
    private static final Map<UUID, Long> lastNotice = new ConcurrentHashMap<>();
    private static final long NOTIFY_COOLDOWN_MS = 5 * 60 * 1000;
    public UpdateChecker(JavaPlugin plugin, int spigotResourceId, String builtByBitUrl) {
        this.plugin = plugin;
        this.spigotResourceId = spigotResourceId;
        this.builtByBitUrl = builtByBitUrl;
        Bukkit.getServicesManager().register(
                UpdateCheckService.class,
                this, plugin,
                org.bukkit.plugin.ServicePriority.Normal
        );
    }
    @Override
    public void checkNow(CommandSender notifyTo, boolean announceIfLatest) {
        checkAndNotifyNow(notifyTo, announceIfLatest);
    }
    public void checkAndNotifyNow(CommandSender sender, boolean announceIfLatest) {
        if (sender instanceof Player p) {
            lastNotice.put(p.getUniqueId(), 0L);
        }
        checkAndNotify(sender, announceIfLatest);
    }
    public void checkAndNotify(CommandSender sender) {
        checkAndNotify(sender, true);
    }
    public void checkAndNotify(CommandSender sender, boolean announceIfLatest) {
        final String local = plugin.getDescription().getVersion();
        CompletableFuture<String> spigotLatest = fetchSpigotLatestVersion(spigotResourceId);
        CompletableFuture<String> bbbLatest    = (builtByBitUrl == null || builtByBitUrl.isBlank())
                ? CompletableFuture.completedFuture(null)
                : fetchBuiltByBitLatestVersion(builtByBitUrl);
        CompletableFuture.allOf(spigotLatest, bbbLatest).thenRunAsync(() -> {
            String latestSpigot = spigotLatest.join();
            String latestBBB    = bbbLatest.join();
            String remoteNewest = newestOf(latestSpigot, latestBBB);
            String latestValue  = buildLatestValue(local, remoteNewest);
            if (remoteNewest == null || compareVersions(local, remoteNewest) >= 0) {
                if (announceIfLatest) sendRunningLatestMessage(sender, local);
            } else {
                sendUpdateMessage(sender, local, latestValue, spigotResourceId, builtByBitUrl);
            }
        }, plugin.getServer().getScheduler().getMainThreadExecutor(plugin));
    }
    private static String centerMono(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        int pad = width - s.length();
        int left = pad / 2;
        int right = pad - left;
        return " ".repeat(left) + s + " ".repeat(right);
    }
    private CompletableFuture<String> fetchSpigotLatestVersion(int resourceId) {
        return CompletableFuture.supplyAsync(() -> {
            String endpoint = "https://api.spiget.org/v2/resources/" + resourceId + "/versions/latest";
            try {
                String json = httpGet(endpoint, 5000);
                Matcher m = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
                if (m.find()) return m.group(1).trim();
            } catch (Exception ignored) {}
            return null;
        });
    }
    private CompletableFuture<String> fetchBuiltByBitLatestVersion(String pageUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String html = httpGet(pageUrl, 6000);
                Pattern p = Pattern.compile("(?:Version|Updated to)\\s*(?:v)?([0-9]+(?:\\.[0-9A-Za-z-]+)*)", Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(html);
                if (m.find()) return m.group(1).trim();
            } catch (Exception ignored) {}
            return null;
        });
    }
    private String httpGet(String url, int timeoutMs) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(timeoutMs);
        con.setReadTimeout(timeoutMs);
        con.setRequestProperty("User-Agent", "NexusCore-UpdateChecker");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            StringBuilder sb = new StringBuilder(2048);
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        }
    }
    private static int compareVersions(String a, String b) {
        if (a == null || a.isBlank()) return -1;
        if (b == null || b.isBlank()) return 1;
        int[] ca = parseCore(a);
        int[] cb = parseCore(b);
        for (int i = 0; i < 3; i++) {
            int d = Integer.compare(ca[i], cb[i]);
            if (d != 0) return d;
        }
        return Integer.compare(preReleaseWeight(a), preReleaseWeight(b));
    }
    private String newestOf(String v1, String v2) {
        if (v1 == null) return v2;
        if (v2 == null) return v1;
        return compareVersions(v1, v2) >= 0 ? v1 : v2;
    }
    private void sendUpdateMessage(CommandSender sender, String current, String latest, int spigotId, String bbbUrl) {
        if (sender instanceof ConsoleCommandSender) {
            sendUpdateMessageConsole(sender, current, latest, spigotId, bbbUrl);
        } else {
            sendUpdateMessageInteractive(sender, current, latest, spigotId, bbbUrl);
        }
    }
    private void sendRunningLatestMessage(CommandSender sender, String current) {
        if (sender instanceof ConsoleCommandSender) {
            sendRunningLatestConsole(sender, current);
        } else {
            sendRunningLatestInteractive(sender, current);
        }
    }
    private void sendUpdateMessageInteractive(CommandSender sender, String current, String latest, int spigotId, String bbbUrl) {
        if (sender instanceof Player player) {
            if (!player.isOp() && !player.hasPermission("nexuscore.admin")) return;
            long now = System.currentTimeMillis();
            long last = lastNotice.getOrDefault(player.getUniqueId(), 0L);
            if (now - last < NOTIFY_COOLDOWN_MS) return;
            lastNotice.put(player.getUniqueId(), now);
        }
        String spigotUrl = (spigotId > 0)
                ? "https://www.spigotmc.org/resources/" + spigotId + "/"
                : "https://www.spigotmc.org/";
        sender.sendMessage(Component.empty());
        sender.sendMessage(Msg.chatRule());
        String msg =
                "<yellow><b>Update available</b></yellow>\n" +
                        "<white>Current:</white> <red><cur></red>\n" +
                        "<white>Latest:</white> <green><lat></green>\n\n" +
                        "<click:open_url:'" + spigotUrl + "'>" +
                        "<hover:show_text:'<aqua>Open SpigotMC download</aqua>'><aqua>[SpigotMC]</aqua></hover></click>" +
                        ((bbbUrl != null && !bbbUrl.isBlank())
                                ? "  <click:open_url:'" + bbbUrl + "'>" +
                                "<hover:show_text:'<light_purple>Open BuiltByBit download</light_purple>'><light_purple>[BuiltByBit]</light_purple></hover></click>"
                                : "");
        Component c = mm.deserialize(msg,
                Placeholder.unparsed("cur", current),
                Placeholder.parsed("lat", latest));
        sender.sendMessage(Component.text().append(Msg.prefix()).append(c));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Msg.chatRule());
    }
    private void sendRunningLatestInteractive(CommandSender sender, String current) {
        if (sender instanceof Player player) {
            if (!player.isOp() && !player.hasPermission("nexuscore.admin")) return;
            long now = System.currentTimeMillis();
            long last = lastNotice.getOrDefault(player.getUniqueId(), 0L);
            if (now - last < NOTIFY_COOLDOWN_MS) return;
            lastNotice.put(player.getUniqueId(), now);
        }
        sender.sendMessage(Component.empty());
        sender.sendMessage(Msg.chatRule());
        String msg =
                "<green><b>Running latest build</b></green>\n" +
                        "<white>Version:</white> <green><cur></green>";
        Component c = mm.deserialize(msg, Placeholder.unparsed("cur", current));
        sender.sendMessage(Component.text().append(Msg.prefix()).append(c));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Msg.chatRule());
    }
    private void sendUpdateMessageConsole(CommandSender sender, String current, String latest, int spigotId, String bbbUrl) {
        if (!plugin.getConfig().getBoolean("updates.enabled", true)) return;
        long now = System.currentTimeMillis();
        if (now - lastConsoleNoticeMs < CONSOLE_DEBOUNCE_MS) return;
        String spigotUrl = (spigotId > 0)
                ? "https://www.spigotmc.org/resources/" + spigotId + "/"
                : "https://www.spigotmc.org/";
        String RULE = Msg.consoleRuleMini();
        String pluginName = plugin.getDescription().getName();
        String pluginVer  = plugin.getDescription().getVersion();
        String brand = pluginName + " v" + pluginVer;
        String centeredBrand = centerMono(brand, RULE_WIDTH);
        String brandLine = "<gradient:#3498DB:#9B59B6><b>" + centeredBrand + "</b></gradient>";
        String block =
                "\n" +
                        RULE + "\n" +
                        brandLine + "\n" +
                        "<yellow><b>Update available:</b></yellow>\n" +
                        "<white>Current:</white> <red><cur></red>\n" +
                        "<white>Latest:</white> <green><lat></green>\n\n" +
                        "<white>SpigotMC:</white> <aqua>" + spigotUrl + "</aqua>\n" +
                        ((bbbUrl != null && !bbbUrl.isBlank())
                                ? "<white>BuiltByBit:</white> <light_purple>" + bbbUrl + "</light_purple>\n"
                                : "") +
                        RULE + "\n";
        Component c = mm.deserialize(block,
                Placeholder.unparsed("cur", current),
                Placeholder.parsed("lat", latest));
        sender.sendMessage(c);
        lastConsoleNoticeMs = now;
    }
    private void sendRunningLatestConsole(CommandSender sender, String current) {
        if (!plugin.getConfig().getBoolean("updates.enabled", true)) return;
        long now = System.currentTimeMillis();
        if (now - lastConsoleNoticeMs < CONSOLE_DEBOUNCE_MS) return;
        String RULE = Msg.consoleRuleMini();
        String pluginName = plugin.getDescription().getName();
        String pluginVer  = plugin.getDescription().getVersion();
        String brand = pluginName + " v" + pluginVer;
        String centeredBrand = centerMono(brand, RULE_WIDTH);
        String brandLine = "<gradient:#3498DB:#9B59B6><b>" + centeredBrand + "</b></gradient>";
        String block =
                "\n" +
                        RULE + "\n" +
                        brandLine + "\n" +
                        "<green><b>Running latest build</b></green>\n" +
                        "<white>Version:</white> <green><cur></green>\n" +
                        RULE + "\n";
        Component c = mm.deserialize(block, Placeholder.unparsed("cur", current));
        sender.sendMessage(c);
        lastConsoleNoticeMs = now;
    }
    private static final Map<String, Integer> PRE_RELEASE_WEIGHT = Map.of(
            "snapshot", -4, "alpha", -3, "a", -3, "beta", -2, "b", -2, "rc", -1
    );
    private static int[] parseCore(String v) {
        String[] parts = v.split("[.-]");
        int[] out = new int[] {0, 0, 0};
        int i = 0;
        for (String p : parts) {
            if (p.matches("\\d+")) {
                out[i++] = Integer.parseInt(p);
                if (i == 3) break;
            } else {
                break;
            }
        }
        return out;
    }
    private static int preReleaseWeight(String v) {
        String lower = v.toLowerCase();
        for (Map.Entry<String, Integer> e : PRE_RELEASE_WEIGHT.entrySet()) {
            if (lower.contains(e.getKey())) return e.getValue();
        }
        return 0;
    }
    private static String buildLatestValue(String local, String remoteCandidate) {
        if (remoteCandidate == null || remoteCandidate.isBlank()
                || compareVersions(local, remoteCandidate) >= 0) {
            return "<green><b>Running latest build</b></green>";
        }
        return remoteCandidate;
    }
}