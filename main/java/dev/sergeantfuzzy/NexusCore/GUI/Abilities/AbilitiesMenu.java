package dev.sergeantfuzzy.NexusCore.GUI.Abilities;

import dev.sergeantfuzzy.NexusCore.Abilities.Ability;
import dev.sergeantfuzzy.NexusCore.Abilities.AbilityManager;
import dev.sergeantfuzzy.NexusCore.GUI.NexusCoreMenu;
import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

public final class AbilitiesMenu implements Listener {
    private static JavaPlugin plugin;
    private static boolean registered = false;
    private static NamespacedKey ITEM_KEY;
    private static final Component TITLE = i18n.mm("<gradient:#F1C40F:#E67E22><bold>Abilities</bold></gradient> <gray>»</gray> Menu");

    private AbilitiesMenu() {}

    public static void open(JavaPlugin plugin, Player player) {
        if (!AbilityManager.isReady()) {
            i18n.sendMM(player, "<red>Abilities are still loading. Try again in a moment.</red>");
            return;
        }
        if (!registered) {
            AbilitiesMenu.plugin = plugin;
            ITEM_KEY = new NamespacedKey(plugin, "ability_id");
            Bukkit.getPluginManager().registerEvents(new AbilitiesMenu(), plugin);
            registered = true;
        }
        Inventory inv = Bukkit.createInventory(new Holder(plugin), inventorySize(), TITLE);
        fillInventory(inv, player);
        player.openInventory(inv);
    }

    private static int inventorySize() {
        int abilityCount = Ability.values().length;
        int rows = (int) Math.ceil(abilityCount / 9.0) + 1;
        rows = Math.max(4, Math.min(6, rows));
        return rows * 9;
    }

    private static void fillInventory(Inventory inv, Player viewer) {
        AbilityManager manager = AbilityManager.instance();
        int slot = 0;
        for (Ability ability : Ability.values()) {
            inv.setItem(slot++, abilityItem(viewer, ability, manager));
        }
        // Navigation row
        int size = inv.getSize();
        inv.setItem(size - 9, navItem(Material.ARROW, "§e← Back", "nexus_menu"));
        inv.setItem(size - 1, navItem(Material.BARRIER, "§cClose", "close"));
    }

    private static ItemStack abilityItem(Player viewer, Ability ability, AbilityManager manager) {
        ItemStack stack = new ItemStack(ability.icon());
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text("§e" + ability.displayName()));
        List<Component> lore = new ArrayList<>();
        for (String line : ability.description()) {
            lore.add(Component.text(line));
        }
        lore.add(Component.text(" "));
        boolean hasPerm = viewer.hasPermission(ability.permissionNode());
        if (!manager.abilitiesEnabled()) {
            lore.add(Component.text("§c↳ Abilities are disabled globally"));
        } else if (!manager.isAbilityEnabled(ability)) {
            lore.add(Component.text("§c↳ This ability is disabled"));
        } else if (!hasPerm) {
            lore.add(Component.text("§c↳ You lack " + ability.permissionNode()));
        } else {
            long remain = manager.remainingCooldownSeconds(viewer, ability);
            if (remain > 0) {
                lore.add(Component.text("§e↳ Cooldown: " + remain + "s"));
            } else {
                lore.add(Component.text("§a↳ Ready to use"));
            }
        }
        lore.add(Component.text("§7Cooldown: §f" + manager.configuredCooldown(ability) + "s"));
        lore.add(Component.text("§7Permission: §f" + ability.permissionNode()));
        meta.lore(lore);
        markAbility(meta, ability.id());
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack navItem(Material material, String name, String action) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name));
        meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, action);
        stack.setItemMeta(meta);
        return stack;
    }

    private static void markAbility(ItemMeta meta, String id) {
        meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, id);
    }

    private static String readAbilityId(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
        if (ITEM_KEY == null) return null;
        return pdc.get(ITEM_KEY, PersistentDataType.STRING);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof Holder)) return;
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) return;
        ClickType click = event.getClick();
        if (!(click.isLeftClick() || click.isRightClick())) return;
        ItemStack clicked = event.getCurrentItem();
        String id = readAbilityId(clicked);
        if (id == null) return;
        if ("close".equals(id)) {
            player.closeInventory();
            return;
        }
        if ("nexus_menu".equals(id)) {
            player.closeInventory();
            NexusCoreMenu.open(plugin, player);
            return;
        }
        Ability ability = Ability.fromInput(id);
        if (ability == null) return;
        if (!player.hasPermission(ability.permissionNode())) {
            i18n.sendMM(player, "<red>You lack permission:</red> <gray>" + ability.permissionNode() + "</gray>");
            return;
        }
        AbilityManager manager = AbilityManager.instance();
        AbilityManager.AbilityResult result = manager.activate(player, ability);
        switch (result.status()) {
            case SUCCESS -> i18n.sendMM(
                    player,
                    Msg.CHAT_PREFIX_MM + "<green>Activated:</green> <yellow>" + ability.displayName() + "</yellow>"
            );
            case DISABLED_GLOBAL -> i18n.sendMM(player, "<red>Abilities are disabled server-wide.</red>");
            case DISABLED_ABILITY -> i18n.sendMM(player, "<yellow>" + ability.displayName() + " is disabled.</yellow>");
            case ON_COOLDOWN -> i18n.sendMM(
                    player,
                    "<yellow>" + ability.displayName() + "</yellow> <gray>is on cooldown for</gray> <gold>" +
                            result.secondsRemaining() + "s</gold>."
            );
        }
        // refresh view to update cooldown text
        Bukkit.getScheduler().runTask(plugin, () -> fillInventory(event.getInventory(), player));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof Holder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Holder)) return;
        HumanEntity entity = event.getPlayer();
        if (entity instanceof Player player) {
            i18n.sendMM(player,
                    Msg.CHAT_PREFIX_MM +
                            "<gray>The </gray><gradient:#F1C40F:#E67E22><bold>Abilities</bold></gradient><gray> menu has been closed.</gray>");
        }
    }

    private static final class Holder implements InventoryHolder {
        private final JavaPlugin owner;
        Holder(JavaPlugin owner) { this.owner = owner; }
        @Override
        public Inventory getInventory() { return null; }
        JavaPlugin owner() { return owner; }
    }
}
