package dev.sergeantfuzzy.NexusCore.GUI.Commands;

import dev.sergeantfuzzy.NexusCore.GUI.NexusCoreMenu;
import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class CommandsMenu implements Listener {
    private static JavaPlugin plugin;
    private static volatile boolean registered = false;
    private static final String TITLE_CORE_MM = "<gradient:#3498DB:#9B59B6><bold>Commands:</bold></gradient> <gray>»</gray> <italic>Core</italic>";
    private static final String TITLE_MAIN_MM = "<gradient:#3498DB:#9B59B6><bold>NexusCore</bold></gradient> <gray>»</gray> Commands";
    private static final String TITLE_ESS_MM = "<gradient:#3498DB:#9B59B6><bold>Commands:</bold></gradient> <gray>»</gray> <italic>Essentials</italic>";
    private static final List<String> ESSENTIALS_SELF_COMMANDS = List.of("feed", "fly", "heal", "health", "jump", "rtp", "back");
    private static final java.util.Map<String, Material> ESS_ICON = java.util.Map.of(
            "feed",   Material.COOKED_BEEF,
            "fly",    Material.FEATHER,
            "heal",   Material.GOLDEN_APPLE,
            "health", Material.RED_DYE,
            "jump",   Material.RABBIT_FOOT,
            "rtp",    Material.ENDER_PEARL,
            "back",   Material.COMPASS
    );
    private static final java.util.Map<String, java.util.List<String>> ESS_LORE = java.util.Map.of(
            "feed", java.util.List.of(
                    "§8§m────────────────────",
                    "§9§lAction§7: §fRefill your hunger & saturation to full.",
                    "§9§lUsage§7: §f/feed",
                    "§9§lContext§7: §fPost-combat, long sprints, or building sessions.",
                    "§9§lNotes§7: §8Won’t grant potion effects; purely restores food.",
                    "§8§m────────────────────",
                    "§a↪ §7Click to run §f/feed"
            ),
            "fly", java.util.List.of(
                    "§8§m────────────────────",
                    "§9§lAction§7: §fToggle flight mode on/off.",
                    "§9§lUsage§7: §f/fly",
                    "§9§lScope§7: §fApplies to your current world/server rules.",
                    "§9§lNotes§7: §8Disabled in some regions/modes per config.",
                    "§8§m────────────────────",
                    "§a↪ §7Click to run §f/fly"
            ),
            "heal", java.util.List.of(
                    "§8§m────────────────────",
                    "§9§lAction§7: §fRestore health to full.",
                    "§9§lUsage§7: §f/heal",
                    "§9§lExtras§7: §fCommon negative effects may be cleared.",
                    "§9§lNotes§7: §8Does not revive or change max health.",
                    "§8§m────────────────────",
                    "§a↪ §7Click to run §f/heal"
            ),
            "health", java.util.List.of(
                    "§8§m────────────────────",
                    "§9§lAction§7: §fDisplay your current health.",
                    "§9§lUsage§7: §f/health",
                    "§9§lContext§7: §fQuick self-check during fights or raids.",
                    "§9§lNotes§7: §8Purely informational; doesn’t alter stats.",
                    "§8§m────────────────────",
                    "§a↪ §7Click to run §f/health"
            ),
            "jump", java.util.List.of(
                    "§8§m────────────────────",
                    "§9§lAction§7: §fTeleport to the block you’re looking at.",
                    "§9§lUsage§7: §f/jump",
                    "§9§lRange§7: §fLine-of-sight; respects region protections.",
                    "§9§lNotes§7: §8Ideal for builders & staff navigation.",
                    "§8§m────────────────────",
                    "§a↪ §7Click to run §f/jump"
            ),
            "back", java.util.List.of(
                    "§8§m────────────────────",
                    "§9§lAction§7: §fReturn to your previous safe location.",
                    "§9§lUsage§7: §f/back [player]",
                    "§9§lContext§7: §fUndo /tp, deaths, or risky teleports.",
                    "§9§lNotes§7: §8Requires a stored location (teleport/death).",
                    "§8§m────────────────────",
                    "§a↪ §7Click to run §f/back"
            ),
            "rtp", java.util.List.of(
                    "§8§m────────────────────",
                    "§9§lAction§7: §fRandomly teleport within the world.",
                    "§9§lUsage§7: §f/rtp",
                    "§9§lScope§7: §fDestination follows server/config rules.",
                    "§9§lNotes§7: §8Use to explore new terrain or exit crowded areas.",
                    "§8§m────────────────────",
                    "§a↪ §7Click to run §f/rtp"
            )
    );
    private CommandsMenu() {}
    public static void init(JavaPlugin pl) {
        if (registered) return;
        synchronized (CommandsMenu.class) {
            if (registered) return;
            plugin = pl;
            Bukkit.getPluginManager().registerEvents(new CommandsMenu(), plugin);
            registered = true;
        }
    }
    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(new Holder(MenuType.MAIN), 27, i18n.mm(TITLE_MAIN_MM));
        // NexusCore commands
        inv.setItem(0, named(Material.BOOK, "§bNexusCore Commands", List.of("§7Open NexusCore command list")));
        // Essentials commands
        inv.setItem(1, named(Material.COMPASS, "§aEssentials Commands", List.of("§7Open Essentials command list")));
        // Close
        inv.setItem(18, backItem("NexusCore Main Menu"));
        inv.setItem(26, named(Material.BARRIER, "❌ §cClose", List.of("§7Close this menu")));
        player.openInventory(inv);
    }
    private static void openEssentials(Player player) {
        Inventory inv = Bukkit.createInventory(new Holder(MenuType.ESSENTIALS), 27, i18n.mm(TITLE_ESS_MM));
        java.util.List<String> cmds = new java.util.ArrayList<>(ESSENTIALS_SELF_COMMANDS);
        cmds.sort(java.util.Comparator.naturalOrder());
        int slot = 0;
        for (String name : cmds) {
            if (slot == 26) break;
            String display = "§a/" + name;
            java.util.List<String> lore = ESS_LORE.getOrDefault(
                    name,
                    java.util.List.of("§7Click to run §f/" + name)
            );
            Material icon = ESS_ICON.getOrDefault(name, Material.PAPER);
            inv.setItem(slot++, named(icon, display, lore));
        }
        inv.setItem(18, backItem("Commands"));
        inv.setItem(26, named(Material.BARRIER, "❌ §cClose", java.util.List.of("§7Close this menu")));
        player.openInventory(inv);
    }
    private static void openCore(Player player) {
        Inventory inv = Bukkit.createInventory(new Holder(MenuType.CORE), 27, i18n.mm(TITLE_CORE_MM));
        int slot = 0;
        inv.setItem(slot++, named(
                Material.PAPER,
                "§a/nexus",
                List.of("§7Click to run §f/nexus")));
        inv.setItem(slot++, named(
                Material.PAPER,
                "§a/nexus reload",
                List.of("§7Click to run §f/nexus reload")));
        inv.setItem(slot, named(
                Material.PAPER,
                "§a/nexus version",
                List.of("§7Click to run §f/nexus version")));
        // Placeholder for future commands
        // inv.setItem(slot++, named(Material.PAPER, "§a/nexus example", List.of("§7Click to run §f/nexus example")));
        inv.setItem(18, backItem("Commands"));
        inv.setItem(26, named(Material.BARRIER, "§cClose", List.of("§7Close this menu")));
        player.openInventory(inv);
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!(e.getView().getTopInventory().getHolder() instanceof Holder holder)) return;
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) {
            e.setCancelled(true);
            return;
        }
        ClickType click = e.getClick();
        if (!(click.isLeftClick() || click.isRightClick()) || click.isShiftClick()
                || click == ClickType.NUMBER_KEY
                || click == ClickType.SWAP_OFFHAND
                || click == ClickType.DROP
                || click == ClickType.CONTROL_DROP
                || click == ClickType.DOUBLE_CLICK) {
            e.setCancelled(true);
            return;
        }
        e.setCancelled(true);
        int raw = e.getRawSlot();
        if (raw < 0 || raw >= e.getView().getTopInventory().getSize()) return;
        if (holder.type == MenuType.MAIN) {
            if (raw == 26) { player.closeInventory(); return; }
            if (raw == 18) { NexusCoreMenu.open(plugin, player); return; }
            if (raw == 0)  { openCore(player);       return; }
            if (raw == 1)  { openEssentials(player); return; }
            return;
        }
        if (holder.type == MenuType.ESSENTIALS) {
            if (raw == 26) { player.closeInventory(); return; }
            if (raw == 18) { open(player); return; }
            ItemStack clicked = e.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR) {
                String cmd = extractCommand(clicked);
                if (cmd != null) runAsPlayer(player, cmd);
            }
            return;
        }
        if (holder.type == MenuType.CORE) {
            if (raw == 26) { player.closeInventory(); return; }
            if (raw == 18) { open(player); return; }
            ItemStack clicked = e.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR) {
                String cmd = extractCommand(clicked);
                if (cmd != null) runAsPlayer(player, cmd);
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!(e.getInventory().getHolder() instanceof Holder)) return;
        e.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (!(inv.getHolder() instanceof Holder)) return;
        HumanEntity he = e.getPlayer();
        if (he instanceof Player p) {
            i18n.sendMM(p, Msg.CHAT_PREFIX_MM +
                    "<gray>The </gray><gradient:#3498DB:#9B59B6><bold>Nexus Core</bold></gradient><gray> commands menu has been closed.</gray> " +
                    "<dark_gray>|</dark_gray> <click:run_command:'/nexus'><hover:show_text:'<green>Click to reopen</green>'>" +
                    "<gradient:#3498DB:#9B59B6><bold>[Reopen]</bold></gradient></hover></click>");
        }
    }
    private static ItemStack backItem(String toMenuName) {
        return named(
                Material.ARROW,
                "§e← Back",
                List.of("§7Return to §f" + toMenuName)
        );
    }
    private static ItemStack named(Material type, String display, List<String> lore) {
        ItemStack it = new ItemStack(type);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Component.text(display));
        if (lore != null && !lore.isEmpty()) {
            List<Component> compLore = new ArrayList<>();
            for (String line : lore) compLore.add(Component.text(line));
            meta.lore(compLore);
        }
        it.setItemMeta(meta);
        return it;
    }
    private static String extractCommand(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;
        Component name = meta.displayName();
        if (name == null) return null;
        String raw = PlainTextComponentSerializer.plainText().serialize(name).trim();
        raw = raw.replaceAll("§[0-9A-FK-ORa-fk-or]", "").trim();
        int slash = raw.indexOf('/');
        if (slash < 0) return null;
        String cmd = raw.substring(slash + 1).trim();
        return cmd.isEmpty() ? null : cmd;
    }
    private static void runAsPlayer(Player player, String commandWithoutSlash) {
        player.performCommand(commandWithoutSlash);
    }
    private enum MenuType { MAIN, ESSENTIALS, CORE }
    private static final class Holder implements InventoryHolder {
        final MenuType type;
        Holder(MenuType t) { this.type = t; }
        @Override public Inventory getInventory() { return null; }
    }
}
