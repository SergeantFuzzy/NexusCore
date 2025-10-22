package dev.sergeantfuzzy.NexusCore.UI.Help;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class GuideBookProtector implements Listener {
    private final JavaPlugin plugin;
    public GuideBookProtector(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        Item dropped = e.getItemDrop();
        ItemStack stack = dropped.getItemStack();
        if (!GuideBookTag.isGuideBook(plugin, stack)) return;
        dropped.setPickupDelay(Integer.MAX_VALUE);
        dropped.setGlowing(true);
        burnAwayDropped(dropped);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack current = e.getCurrentItem();
        ItemStack cursor  = e.getCursor();
        boolean currentIsBook = GuideBookTag.isGuideBook(plugin, current);
        boolean cursorIsBook  = GuideBookTag.isGuideBook(plugin, cursor);
        if (!currentIsBook && !cursorIsBook) return;
        Inventory clickedInv = e.getClickedInventory();
        InventoryView view = e.getView();
        boolean topInventory = (clickedInv != null && clickedInv.equals(view.getTopInventory()));
        boolean moveToOther  = e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY;
        boolean dropping = e.getAction() == InventoryAction.DROP_ALL_SLOT
                || e.getAction() == InventoryAction.DROP_ONE_SLOT
                || e.getClick()  == ClickType.DROP
                || e.getClick()  == ClickType.CONTROL_DROP;
        boolean movingOut = topInventory || moveToOther || dropping;
        if (movingOut) {
            e.setCancelled(true);
            if (currentIsBook && e.getCurrentItem() != null) {
                e.setCurrentItem(null);
            }
            if (cursorIsBook && e.getCursor() != null) {
                e.setCursor(null);
            }
            burnAtPlayerAndConfirmRemoval(p);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack cursor = e.getOldCursor();
        if (!GuideBookTag.isGuideBook(plugin, cursor)) return;
        InventoryView view = e.getView();
        int topSize = view.getTopInventory().getSize();
        boolean intoTop = e.getRawSlots().stream().anyMatch(raw -> raw < topSize);
        if (intoTop) {
            e.setCancelled(true);
            e.setCursor(null);
            burnAtPlayerAndConfirmRemoval(p);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent e) {
        ItemStack main = e.getMainHandItem();
        ItemStack off  = e.getOffHandItem();
        if (GuideBookTag.isGuideBook(plugin, main) || GuideBookTag.isGuideBook(plugin, off)) {
            e.setCancelled(true);
            if (GuideBookTag.isGuideBook(plugin, main)) e.getPlayer().getInventory().setItemInMainHand(null);
            if (GuideBookTag.isGuideBook(plugin, off))  e.getPlayer().getInventory().setItemInOffHand(null);
            burnAtPlayerAndConfirmRemoval(e.getPlayer());
        }
    }
    private void burnAwayDropped(Item itemEntity) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (itemEntity.isDead() || !itemEntity.isValid()) {
                    cancel(); return;
                }
                World w = itemEntity.getWorld();
                var loc = itemEntity.getLocation().add(0, 0.1, 0);
                w.spawnParticle(Particle.LARGE_SMOKE, loc, 3, 0.2, 0.15, 0.2, 0.01);
                w.spawnParticle(Particle.ASH,         loc, 6, 0.3, 0.20, 0.3, 0.00);
                w.spawnParticle(Particle.LAVA,        loc, 1, 0.1, 0.00, 0.1, 0.00);
                w.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.7f, 1.2f);
                ticks += 5;
                if (ticks >= 60) {
                    itemEntity.remove();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
    private void burnAtPlayerAndConfirmRemoval(Player p) {
        var loc = p.getLocation().add(0, 1.0, 0);
        World w = p.getWorld();
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (!p.isOnline()) { cancel(); return; }
                w.spawnParticle(Particle.LARGE_SMOKE, loc, 4, 0.25, 0.20, 0.25, 0.01);
                w.spawnParticle(Particle.ASH,         loc, 8, 0.35, 0.25, 0.35, 0.00);
                w.spawnParticle(Particle.FLAME,       loc, 1, 0.10, 0.05, 0.10, 0.00);
                w.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.8f, 1.15f);
                ticks += 5;
                if (ticks >= 60) cancel();
            }
        }.runTaskTimer(plugin, 0L, 5L);
        purgeGuideBooksFromInventory(p);
    }
    private void purgeGuideBooksFromInventory(Player p) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack slot = inv.getItem(i);
            if (GuideBookTag.isGuideBook(plugin, slot)) {
                inv.clear(i);
            }
        }
        if (GuideBookTag.isGuideBook(plugin, p.getItemOnCursor())) {
            p.setItemOnCursor(null);
        }
    }
}