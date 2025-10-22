package dev.sergeantfuzzy.NexusCore.UI.Help;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class GuideBookTag {
    private GuideBookTag() {}
    public static NamespacedKey key(JavaPlugin plugin) {
        return new NamespacedKey(plugin, "guide_book");
    }
    public static ItemStack tag(JavaPlugin plugin, ItemStack book) {
        if (book == null) return null;
        ItemMeta meta = book.getItemMeta();
        if (meta == null) return book;
        meta.getPersistentDataContainer().set(key(plugin), PersistentDataType.BYTE, (byte)1);
        book.setItemMeta(meta);
        return book;
    }
    public static boolean isGuideBook(JavaPlugin plugin, ItemStack stack) {
        if (stack == null) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Byte flag = pdc.get(key(plugin), PersistentDataType.BYTE);
        return flag != null && flag == (byte)1;
    }
}