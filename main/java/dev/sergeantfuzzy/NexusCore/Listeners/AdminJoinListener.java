package dev.sergeantfuzzy.NexusCore.Listeners;

import dev.sergeantfuzzy.NexusCore.UI.Help.GuideBookProtector;
import dev.sergeantfuzzy.NexusCore.UI.Help.GuideBookTag;
import dev.sergeantfuzzy.NexusCore.UI.Help.HelpBookUI;
import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.UpdateChecker;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdminJoinListener implements Listener {
    private final JavaPlugin plugin;
    private static final String PERM = "nexuscore.guidebook";
    private final UpdateChecker updateChecker;
    private static final String CFG_GUIDE_ENABLED = "guidebook.enabled";
    public AdminJoinListener(JavaPlugin plugin, int spigotResourceId, String builtByBitUrl) {
        this.plugin = plugin;
        this.updateChecker = new UpdateChecker(plugin, spigotResourceId, builtByBitUrl);
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new GuideBookProtector(plugin), plugin);
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        boolean guideOn = guideBookEnabled(plugin);
        if (guideOn && isAdmin(p)) {
            Integer slot = findGuideBookSlot(p);
            if (slot != null) {
                if (isEmpty(p.getInventory().getItemInMainHand())) {
                    ItemStack existing = p.getInventory().getItem(slot);
                    p.getInventory().setItemInMainHand(existing);
                    p.getInventory().clear(slot);
                }
            } else {
                ItemStack book = HelpBookUI.createPhysicalBook(plugin, p);
                book = GuideBookTag.tag(plugin, book);

                ItemStack inMain = p.getInventory().getItemInMainHand();
                if (isEmpty(inMain)) {
                    p.getInventory().setItemInMainHand(book);
                } else {
                    p.getInventory().addItem(book);
                }
            }
        } else {
            removeTaggedGuideBooks(p);
        }
        if (!(p.isOp() || p.hasPermission("nexuscore.admin"))) return;
        Component info = Msg.buildAdminJoinLine(plugin);
        Msg.sendPrefixed(p, info);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> updateChecker.checkAndNotify(p));
    }
    private static boolean isAdmin(Player p) {
        return p.isOp() || p.hasPermission(PERM);
    }
    private static boolean isEmpty(ItemStack s) {
        return s == null || s.getType().isAir();
    }
    private static boolean parseBoolLoose(Object o) {
        if (o instanceof Boolean b) return b;
        String s = String.valueOf(o).trim();
        return s.equalsIgnoreCase("true")
                || s.equalsIgnoreCase("yes")
                || s.equalsIgnoreCase("on")
                || s.equals("1");
    }
    private static boolean guideBookEnabled(JavaPlugin plugin) {
        FileConfiguration cfg = plugin.getConfig();
        if (cfg.isSet(CFG_GUIDE_ENABLED)) {
            return cfg.getBoolean(CFG_GUIDE_ENABLED, true);
        }
        String[] legacyKeys = {"GuideBook", "guideBook", "guidebook"};
        for (String k : legacyKeys) {
            if (cfg.isSet(k)) {
                boolean v = parseBoolLoose(cfg.get(k));
                cfg.set(CFG_GUIDE_ENABLED, v);
                cfg.set(k, null);
                plugin.saveConfig();
                return v;
            }
        }
        cfg.set(CFG_GUIDE_ENABLED, true);
        plugin.saveConfig();
        return true;
    }
    private Integer findGuideBookSlot(Player p) {
        if (GuideBookTag.isGuideBook(plugin, p.getInventory().getItemInMainHand())) return p.getInventory().getHeldItemSlot();
        if (GuideBookTag.isGuideBook(plugin, p.getInventory().getItemInOffHand())) return 40;
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack it = p.getInventory().getItem(i);
            if (GuideBookTag.isGuideBook(plugin, it)) return i;
        }
        return null;
    }
    private void removeTaggedGuideBooks(Player p) {
        ItemStack[] contents = p.getInventory().getContents();
        boolean changed = false;
        for (int i = 0; i < contents.length; i++) {
            ItemStack it = contents[i];
            if (it == null || it.getType().isAir()) continue;
            if (GuideBookTag.isGuideBook(plugin, it)) {
                contents[i] = null;
                changed = true;
            }
        }
        if (changed) p.getInventory().setContents(contents);
    }
}