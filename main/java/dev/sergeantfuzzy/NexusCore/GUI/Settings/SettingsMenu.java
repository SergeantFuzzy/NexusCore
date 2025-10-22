package dev.sergeantfuzzy.NexusCore.GUI.Settings;

import dev.sergeantfuzzy.NexusCore.GUI.NexusCoreMenu;
import dev.sergeantfuzzy.NexusCore.UI.Help.GuideBookTag;
import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class SettingsMenu {
    private SettingsMenu() {}
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Component TITLE = MM.deserialize("<gradient:#3498DB:#9B59B6><bold>NexusCore</bold></gradient> <gray>»</gray> Settings");
    private static final String CFG_UPDATES_ENABLED = "updates.enabled";
    private static volatile boolean LISTENER_REGISTERED = false;
    private static final String CFG_GUIDE_ENABLED = "guidebook.enabled";
    private static final int SLOT_GUIDE_TOGGLE = 1;
    private static final String CFG_SCOREBOARD_ENABLED = "System.Scoreboard";
    private static final String CFG_TABLIST_ENABLED    = "System.Tablist";
    private static final int SLOT_SCOREBOARD_TOGGLE = 2;
    private static final int SLOT_TABLIST_TOGGLE    = 3;
    public static void open(JavaPlugin plugin, Player player) {
        ensureListenerRegistered(plugin);
        if (!plugin.getConfig().isSet(CFG_UPDATES_ENABLED)) plugin.getConfig().set(CFG_UPDATES_ENABLED, true);
        if (!plugin.getConfig().isSet(CFG_GUIDE_ENABLED))   plugin.getConfig().set(CFG_GUIDE_ENABLED, true);
        if (!plugin.getConfig().isSet(CFG_SCOREBOARD_ENABLED)) plugin.getConfig().set(CFG_SCOREBOARD_ENABLED, true);
        if (!plugin.getConfig().isSet(CFG_TABLIST_ENABLED))    plugin.getConfig().set(CFG_TABLIST_ENABLED, true);
        plugin.saveConfig();
        Inventory inv = Bukkit.createInventory(new Holder(plugin), 18, TITLE);
        boolean enabled = plugin.getConfig().getBoolean(CFG_UPDATES_ENABLED, true);
        inv.setItem(0, buildUpdateToggleItem(enabled));
        inv.setItem(SLOT_GUIDE_TOGGLE, buildGuideToggleItem(plugin));
        inv.setItem(SLOT_SCOREBOARD_TOGGLE, buildScoreboardToggleItem(plugin));
        inv.setItem(SLOT_TABLIST_TOGGLE,    buildTablistToggleItem(plugin));
        inv.setItem(9,  buildBackItem());
        inv.setItem(17, buildCloseItem());
        fillPadding(inv);
        player.openInventory(inv);
    }
    private static ItemStack buildUpdateToggleItem(boolean enabled) {
        Material mat = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(MM.deserialize(
                enabled
                        ? "<green><bold>Update Check</bold> <gray>»</gray> <white>ON</white></green>"
                        : "<gray><bold>Update Check</bold> <dark_gray>»</dark_gray> <white>OFF</white></gray>"
        ));
        List<Component> lore = new ArrayList<>();
        lore.add(MM.deserialize("<gray>Enable/disable automatic update notifications.</gray>"));
        lore.add(MM.deserialize("<dark_gray>Click to toggle.</dark_gray>"));
        meta.lore(lore);
        it.setItemMeta(meta);
        return it;
    }
    private static ItemStack buildGuideToggleItem(JavaPlugin plugin) {
        boolean enabled = plugin.getConfig().getBoolean(CFG_GUIDE_ENABLED, true);
        ItemStack stack = new ItemStack(Material.BOOK);
        ItemMeta meta = stack.getItemMeta();
        String title = enabled
                ? "<green><bold>Guide Book</bold></green> <gray>—</gray> <white>Enabled</white>"
                : "<red><bold>Guide Book</bold></red> <gray>—</gray> <white>Disabled</white>";
        meta.displayName(MM.deserialize(title));
        meta.lore(List.of(
                MM.deserialize("<gray>Toggle the player help guide book.</gray>"),
                MM.deserialize(enabled ? "<green>Currently: Enabled</green>" : "<red>Currently: Disabled</red>"),
                MM.deserialize("<dark_gray>Click to toggle.</dark_gray>")
        ));
        if (enabled) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        stack.setItemMeta(meta);
        return stack;
    }
    private static ItemStack buildScoreboardToggleItem(JavaPlugin plugin) {
        boolean enabled = plugin.getConfig().getBoolean(CFG_SCOREBOARD_ENABLED, true);
        ItemStack stack = new ItemStack(enabled ? Material.GLOW_ITEM_FRAME : Material.ITEM_FRAME);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(MM.deserialize(
                enabled
                        ? "<green><bold>Scoreboard</bold></green> <gray>—</gray> <white>Enabled</white>"
                        : "<red><bold>Scoreboard</bold></red> <gray>—</gray> <white>Disabled</white>"
        ));
        meta.lore(List.of(
                MM.deserialize("<gray>Toggle the right-side sidebar scoreboard.</gray>"),
                MM.deserialize(enabled ? "<green>Currently: Enabled</green>" : "<red>Currently: Disabled</red>"),
                MM.deserialize("<dark_gray>Click to toggle.</dark_gray>")
        ));
        if (enabled) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        stack.setItemMeta(meta);
        return stack;
    }
    private static ItemStack buildTablistToggleItem(JavaPlugin plugin) {
        boolean enabled = plugin.getConfig().getBoolean(CFG_TABLIST_ENABLED, true);
        ItemStack stack = new ItemStack(enabled ? Material.NAME_TAG : Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(MM.deserialize(
                enabled
                        ? "<green><bold>Tablist</bold></green> <gray>—</gray> <white>Enabled</white>"
                        : "<red><bold>Tablist</bold></red> <gray>—</gray> <white>Disabled</white>"
        ));
        meta.lore(List.of(
                MM.deserialize("<gray>Toggle the Tab header/footer styling.</gray>"),
                MM.deserialize(enabled ? "<green>Currently: Enabled</green>" : "<red>Currently: Disabled</red>"),
                MM.deserialize("<dark_gray>Click to toggle.</dark_gray>")
        ));
        if (enabled) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        stack.setItemMeta(meta);
        return stack;
    }
    private static ItemStack buildBackItem() {
        ItemStack it = new ItemStack(Material.ARROW);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(MM.deserialize("<yellow>← <bold>Back</bold></yellow>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MM.deserialize("<gray>Return to </gray><white>Nexus Core</white>"));
        meta.lore(lore);
        it.setItemMeta(meta);
        return it;
    }
    private static ItemStack buildCloseItem() {
        ItemStack it = new ItemStack(Material.BARRIER);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(MM.deserialize("❌ <red><bold>Close</bold></red>"));
        List<Component> lore = new ArrayList<>();
        lore.add(MM.deserialize("<gray>Close this menu.</gray>"));
        meta.lore(lore);
        it.setItemMeta(meta);
        return it;
    }
    private static void toggleAndRefresh(JavaPlugin plugin, Inventory inv, Player player) {
        boolean current = plugin.getConfig().getBoolean(CFG_UPDATES_ENABLED, true);
        boolean next = !current;
        plugin.getConfig().set(CFG_UPDATES_ENABLED, next);
        plugin.saveConfig();
        inv.setItem(0, buildUpdateToggleItem(next));
        player.playSound(player.getLocation(),
                next ? Sound.UI_BUTTON_CLICK : Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, next ? 1.35f : 0.75f);
        String msg = next
                ? "<white><bold>Update check</bold></white><gray> is now </gray><green><bold>ENABLED</bold></green><gray>.</gray>"
                : "<gray><bold>Update check</bold> is now </gray><red><bold>DISABLED</bold></red><gray>.</gray>";
        i18n.sendMM(player, Msg.CHAT_PREFIX_MM + msg);
        if (next) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                var svc = Bukkit.getServicesManager()
                        .load(dev.sergeantfuzzy.NexusCore.Update.UpdateCheckService.class);
                if (svc != null) {
                    svc.checkNow(player, true);
                }
            });
        }
    }
    private static final class Holder implements InventoryHolder {
        final JavaPlugin plugin;
        Holder(JavaPlugin plugin) { this.plugin = plugin; }
        @Override public Inventory getInventory() { return null; }
    }
    private static final class ClickListener implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent e) {
            if (!(e.getWhoClicked() instanceof Player player)) return;
            if (!(e.getInventory().getHolder() instanceof Holder holder)) return;
            if (e.getClickedInventory() == null) return;
            if (e.getRawSlot() >= e.getView().getTopInventory().getSize()) return;
            e.setCancelled(true);
            JavaPlugin plugin = holder.plugin;
            final int slot = e.getRawSlot();
            if (slot == 0) {
                toggleAndRefresh(plugin, e.getInventory(), player);
                return;
            }
            if (slot == SLOT_GUIDE_TOGGLE) {
                boolean was = plugin.getConfig().getBoolean(CFG_GUIDE_ENABLED, false);
                boolean now = !was;
                plugin.getConfig().set(CFG_GUIDE_ENABLED, now);
                plugin.saveConfig();
                e.getInventory().setItem(SLOT_GUIDE_TOGGLE, buildGuideToggleItem(plugin));
                if (!now) {
                    removeTaggedGuideBooks(plugin, player);
                }
                String msg = Msg.CHAT_PREFIX_MM + (now
                        ? "<green>Guide Book is now <bold>ENABLED</bold>.</green>"
                        : "<red>Guide Book is now <bold>DISABLED</bold>.</red>");
                player.sendMessage(MM.deserialize(msg));
                player.playSound(player.getLocation(),
                        now ? Sound.UI_BUTTON_CLICK : Sound.BLOCK_NOTE_BLOCK_BASS,
                        1f, now ? 1.2f : 0.8f);
                return;
            }
            if (slot == SLOT_SCOREBOARD_TOGGLE) {
                boolean was = plugin.getConfig().getBoolean(CFG_SCOREBOARD_ENABLED, true);
                boolean now = !was;
                plugin.getConfig().set(CFG_SCOREBOARD_ENABLED, now);
                plugin.saveConfig();
                e.getInventory().setItem(SLOT_SCOREBOARD_TOGGLE, buildScoreboardToggleItem(plugin));
                try {
                    dev.sergeantfuzzy.NexusCore.System.Scoreboard.Scoreboard.reloadAndApply();
                } catch (Throwable t) {
                    plugin.getLogger().warning("[SettingsMenu] Failed to apply Scoreboard toggle: " + t.getMessage());
                }
                String msg = Msg.CHAT_PREFIX_MM + (now
                        ? "<green>Scoreboard is now <bold>ENABLED</bold>.</green>"
                        : "<red>Scoreboard is now <bold>DISABLED</bold>.</red>");
                player.sendMessage(MM.deserialize(msg));
                player.playSound(player.getLocation(),
                        now ? Sound.UI_BUTTON_CLICK : Sound.BLOCK_NOTE_BLOCK_BASS,
                        1f, now ? 1.2f : 0.8f);
                return;
            }
            if (slot == SLOT_TABLIST_TOGGLE) {
                boolean was = plugin.getConfig().getBoolean(CFG_TABLIST_ENABLED, true);
                boolean now = !was;
                plugin.getConfig().set(CFG_TABLIST_ENABLED, now);
                plugin.saveConfig();
                e.getInventory().setItem(SLOT_TABLIST_TOGGLE, buildTablistToggleItem(plugin));
                try {
                    dev.sergeantfuzzy.NexusCore.System.Tablist.Tablist.reloadAndApply();
                } catch (Throwable t) {
                    plugin.getLogger().warning("[SettingsMenu] Failed to apply Tablist toggle: " + t.getMessage());
                }
                String msg = Msg.CHAT_PREFIX_MM + (now
                        ? "<green>Tablist is now <bold>ENABLED</bold>.</green>"
                        : "<red>Tablist is now <bold>DISABLED</bold>.</red>");
                player.sendMessage(MM.deserialize(msg));
                player.playSound(player.getLocation(),
                        now ? Sound.UI_BUTTON_CLICK : Sound.BLOCK_NOTE_BLOCK_BASS,
                        1f, now ? 1.2f : 0.8f);
                return;
            }
            if (slot == 9) {
                NexusCoreMenu.open(plugin, player);
            }
            if (slot == 17) {
                player.closeInventory();
            }
        }
        @EventHandler
        public void onClose(InventoryCloseEvent e) {
            if (!(e.getInventory().getHolder() instanceof Holder)) return;
            Player p = (Player) e.getPlayer();
            i18n.sendMM(p, Msg.CHAT_PREFIX_MM +
                    "<gray>Closed </gray><gradient:#3498DB:#9B59B6><b>Nexus Core</b></gradient><gray> settings menu.</gray> " +
                    "<dark_gray>|</dark_gray> <click:run_command:'/nexus'><hover:show_text:'<green>Click to reopen</green>'>" +
                    "<gradient:#3498DB:#9B59B6><bold>[Reopen]</bold></gradient></hover></click>");
        }
        @EventHandler
        public void onDrag(org.bukkit.event.inventory.InventoryDragEvent e) {
            if (!(e.getWhoClicked() instanceof Player)) return;
            if (!(e.getInventory().getHolder() instanceof Holder)) return;
            e.setCancelled(true);
        }
    }
    private static void ensureListenerRegistered(JavaPlugin plugin) {
        if (LISTENER_REGISTERED) return;
        synchronized (SettingsMenu.class) {
            if (LISTENER_REGISTERED) return;
            PluginManager pm = Bukkit.getPluginManager();
            pm.registerEvents(new ClickListener(), plugin);
            LISTENER_REGISTERED = true;
        }
    }
    @SuppressWarnings("unused")
    private static void fillPadding(Inventory inv) {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.text(" "));
        pane.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, pane);
        }
    }
    private static void removeTaggedGuideBooks(JavaPlugin plugin, Player p) {
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