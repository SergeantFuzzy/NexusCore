package dev.sergeantfuzzy.NexusCore.GUI.Settings;

import dev.sergeantfuzzy.NexusCore.GUI.NexusCoreMenu;
import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n; // adjust if your i18n path differs
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SettingsMenu {
    private SettingsMenu() {}
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Component TITLE = MM.deserialize("<gradient:#3498DB:#9B59B6><bold>NexusCore</bold></gradient> <gray>»</gray> Settings");
    private static final String CFG_UPDATES_ENABLED = "updates.enabled";
    private static volatile boolean LISTENER_REGISTERED = false;
    public static void open(JavaPlugin plugin, Player player) {
        ensureListenerRegistered(plugin);
        if (!plugin.getConfig().isSet(CFG_UPDATES_ENABLED)) {
            plugin.getConfig().set(CFG_UPDATES_ENABLED, true);
            plugin.saveConfig();
        }
        Inventory inv = Bukkit.createInventory(new Holder(plugin), 18, TITLE);
        boolean enabled = plugin.getConfig().getBoolean(CFG_UPDATES_ENABLED, true);
        inv.setItem(0, buildUpdateToggleItem(enabled));
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
            e.setCancelled(true);
            if (e.getClickedInventory() == null) return;
            if (!Objects.equals(e.getClickedInventory(), e.getInventory())) return;
            int slot = e.getSlot();
            if (slot == 0) {
                toggleAndRefresh(holder.plugin, e.getInventory(), player);
            }
            if (slot == 9) {
                NexusCoreMenu.open(holder.plugin, player);
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
}