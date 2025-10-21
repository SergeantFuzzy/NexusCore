package dev.sergeantfuzzy.NexusCore.GUI;

import dev.sergeantfuzzy.NexusCore.GUI.Commands.CommandsMenu;
import dev.sergeantfuzzy.NexusCore.GUI.Settings.SettingsMenu;
import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class NexusCoreMenu implements Listener {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int SIZE = 9;
    private static final Component TITLE = MM.deserialize("<gradient:#3498DB:#9B59B6><bold>NexusCore</bold></gradient> <gray>»</gray> Main Menu");
    private static volatile boolean registered = false;
    private static NamespacedKey INV_KEY;
    private static NamespacedKey ACTION_KEY;
    private final Plugin plugin;
    private NexusCoreMenu(Plugin plugin) {
        this.plugin = plugin;
    }
    private static final class Holder implements InventoryHolder {
        private final Plugin plugin;
        Holder(Plugin plugin) { this.plugin = plugin; }
        @Override public Inventory getInventory() { return null; }
        Plugin plugin() { return plugin; }
    }
    public static void open(JavaPlugin plugin, Player player) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(player, "player");
        if (INV_KEY == null) {
            INV_KEY = new NamespacedKey(plugin, "nexus_menu");
            ACTION_KEY = new NamespacedKey(plugin, "nexus_menu_action");
        }
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new NexusCoreMenu(plugin), plugin);
            registered = true;
        }
        Inventory inv = Bukkit.createInventory(new Holder(plugin), SIZE, TITLE);
        tagInventory(inv);
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, filler(i));
        }
        inv.setItem(2, comingSoonItem(
                Material.NETHER_STAR,
                "<yellow><bold>Abilities</bold></yellow>",
                List.of(
                        "<gray>Customize passive and active abilities.</gray>",
                        "<dark_gray>Coming soon.</dark_gray>"
                ),
                "abilities"
        ));
        inv.setItem(4, comingSoonItem(
                Material.BOOK,
                "<aqua><bold>Commands</bold></aqua>",
                List.of(
                        "<gray>Browse and run core commands.</gray>"
                ),
                "commands"
        ));
        inv.setItem(6, comingSoonItem(
                Material.COMPARATOR,
                "<green><bold>Settings</bold></green>",
                List.of(
                        "<gray>Adjust Nexus Core preferences.</gray>"
                ),
                "settings"
        ));
        inv.setItem(8, actionItem(
                Material.BARRIER,
                "<red>❌ <bold>Close Menu</bold></red>",
                List.of(
                        "<gray>Exit the Nexus Core menu.</gray>",
                        "<dark_gray>Click to close.</dark_gray>"
                ),
                "close"
        ));
        player.openInventory(inv);
    }
    private static void tagInventory(Inventory inv) {
    }
    private static ItemStack filler(int index) {
        ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Component.text(" "));
        meta.addItemFlags(ItemFlag.values());
        it.setItemMeta(meta);
        addGlow(it);
        markAction(it, "filler");
        return it;
    }
    private static ItemStack comingSoonItem(Material mat, String nameMini, List<String> loreMini, String action) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(MM.deserialize(nameMini));
        meta.lore(deserializeLore(loreMini));
        meta.addItemFlags(ItemFlag.values());
        it.setItemMeta(meta);
        addGlow(it);
        markAction(it, action);
        return it;
    }
    private static ItemStack actionItem(Material mat, String nameMini, List<String> loreMini, String action) {
        return comingSoonItem(mat, nameMini, loreMini, action);
    }
    private static List<Component> deserializeLore(List<String> lines) {
        List<Component> out = new ArrayList<>(lines.size());
        for (String l : lines) out.add(MM.deserialize(l));
        return out;
    }
    private static void addGlow(ItemStack it) {
        ItemMeta meta = it.getItemMeta();
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        it.setItemMeta(meta);
    }
    private static void markAction(ItemStack it, String action) {
        ItemMeta meta = it.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(ACTION_KEY, PersistentDataType.STRING, action);
        it.setItemMeta(meta);
    }
    private static String readAction(ItemStack it) {
        if (it == null || !it.hasItemMeta()) return null;
        PersistentDataContainer pdc = it.getItemMeta().getPersistentDataContainer();
        return pdc.get(ACTION_KEY, PersistentDataType.STRING);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!(e.getInventory().getHolder() instanceof Holder)) return;
        e.setCancelled(true);
        if (e.getClickedInventory() == null || !Objects.equals(e.getClickedInventory().getHolder(), e.getInventory().getHolder())) {
            return;
        }
        ClickType type = e.getClick();
        if (!(type.isLeftClick() || type.isRightClick())) {
            return;
        }
        ItemStack clicked = e.getCurrentItem();
        String action = readAction(clicked);
        if (action == null) {
            return;
        }
        switch (action) {
            case "close" -> {
                player.closeInventory();
            }
            case "abilities" -> {
                i18n.sendMM(player, "<gray>That section is </gray><yellow><bold>coming soon</bold></yellow><gray>.</gray>");
            }
            case "commands" -> {
                CommandsMenu.init((JavaPlugin) plugin);
                CommandsMenu.open(player);
            }case "settings" -> {
                SettingsMenu.open((JavaPlugin) plugin, player);
            }
            default -> {
            }
        }
    }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!(e.getInventory().getHolder() instanceof Holder)) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        HumanEntity who = e.getPlayer();
        if (!(who instanceof Player player)) return;
        if (!(e.getInventory().getHolder() instanceof Holder)) return;
        sendClosedNotice(player);
    }
    private static void sendClosedNotice(Player player) {
        String body = """
        <gray>The </gray><gradient:#3498DB:#9B59B6><bold>Nexus Core</bold></gradient><gray> menu has been closed.</gray> \
        <dark_gray>|</dark_gray> \
        <click:run_command:'/nexus'><hover:show_text:'<green>Click to reopen</green>'>\
        <gradient:#3498DB:#9B59B6><bold>[Reopen]</bold></gradient></hover></click>
        """;
        i18n.sendMM(player, Msg.CHAT_PREFIX_MM + body);
    }
    public static void unregister(JavaPlugin plugin) {
        if (registered) {
            HandlerList.unregisterAll(plugin);
            registered = false;
        }
    }
}