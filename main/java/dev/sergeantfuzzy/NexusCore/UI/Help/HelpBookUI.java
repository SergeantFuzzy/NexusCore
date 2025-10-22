package dev.sergeantfuzzy.NexusCore.UI.Help;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class HelpBookUI {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private HelpBookUI() {}
    private static Component title(String subtitle) {
        String mm = "<gradient:#3498DB:#9B59B6><bold>NexusCore</bold></gradient> <gray>»</gray> " + subtitle;
        return MM.deserialize(mm);
    }
    private static Component author(JavaPlugin plugin) {
        String mm = "<gray>" + plugin.getDescription().getName() + "</gray>";
        return MM.deserialize(mm);
    }
    private static Component page(String miniMessage) {
        return MM.deserialize(miniMessage);
    }
    public static Book coreOverview(JavaPlugin plugin) {
        List<Component> pages = new ArrayList<>();
        pages.add(page(String.join("\n",
                "<hover:show_text:'<gray>The core framework powering your server’s commands, menus, and update system.</gray>'><gradient:#3498DB:#9B59B6><bold>Welcome to NexusCore</bold></gradient></hover>",
                "",
                "<dark_gray>—</dark_gray> <blue><bold>Quick Actions</bold></blue>",
                " <click:run_command:'/nexus'><hover:show_text:'<green>Open NexusCore menu</green>'><gradient:#3498DB:#9B59B6>[Open Menu]</gradient></hover></click>",
                " <click:run_command:'/nexus version'><hover:show_text:'<green>Show version & links</green>'><gradient:#3498DB:#9B59B6>[Version]</gradient></hover></click>",
                "",
                "<dark_gray>—</dark_gray> <blue><bold>Sections</blue>",
                " <click:run_command:'/nexushelp commands'><hover:show_text:'<green>See command reference</green>'><dark_aqua>[Commands]</dark_aqua></hover></click> " +
                        "<click:run_command:'/nexushelp links'><hover:show_text:'<green>Useful links (Spigot, BBB, Discord)</green>'><dark_aqua>[Links]</dark_aqua></hover></click> " +
                        "<click:run_command:'/nexushelp perms'><hover:show_text:'<green>Permission nodes</green>'><dark_aqua>[Permissions]</dark_aqua></hover></click>",
                "",
                "<dark_gray>Tip:</dark_gray> <gray><bold>Click</bold> buttons or text in this book.</gray>"
        )));
        pages.add(page(String.join("\n",
                "<dark_gray>—</dark_gray> <blue><bold>Basics</bold></blue>",
                "<hover:show_text:'<gray>Use</gray> <dark_aqua>/nexus</dark_aqua> <gray>to open the main GUI.</gray>'> <gray>•</gray> <click:run_command:'/nexus'><dark_gray>NexusCore Menu</dark_gray></click></hover>",
                "<hover:show_text:'<gray>Use</gray> <dark_aqua>/nexus reload</dark_aqua> <gray>to reload configs/messages.</gray>'> <gray>•</gray> <click:run_command:'/nexus reload'><dark_gray>Reload NexusCore</dark_gray></click></hover>",
                "<hover:show_text:'<gray>Use</gray> <dark_aqua>/nexus version</dark_aqua> <gray>for version/build info.</gray>'> <gray>•</gray> <click:run_command:'/nexus version'><dark_gray>Check Version</dark_gray></click></hover>",
                "",
                "<dark_gray>—</dark_gray> <blue><bold>Support</bold></blue>",
                " <click:open_url:'https://www.spigotmc.org/'><hover:show_text:'<green>Open SpigotMC</green>'><gradient:#3498DB:#9B59B6>[SpigotMC]</gradient></hover></click> " +
                        " <click:open_url:'https://builtbybit.com/'><hover:show_text:'<green>Open BuiltByBit</green>'><gradient:#3498DB:#9B59B6>[BBB]</gradient></hover></click>",
                " <click:suggest_command:'/help nexuscore'><hover:show_text:'<green>Suggest NexusCore help command in chat</green>'><gradient:#3498DB:#9B59B6>[Help Me!]</gradient></hover></click>",
                "",
                navRow("commands", "links", null)
        )));
        return Book.book(
                title("<gray>Help</gray>"),
                author(plugin),
                pages
        );
    }
    public static Book commands(JavaPlugin plugin) {
        List<Component> pages = new ArrayList<>();
        pages.add(page(String.join("\n",
                "<hover:show_text:'<gradient:#3498DB:#9B59B6><bold>Note:</bold></gradient>\n<gray>The complete list of all available commands</gray>\n<gray>will not be displayed in this guide. To view</gray>\n<gray>the complete list, visit the <bold>SpigotMC</bold> or</gray>\n<gray><bold>BuiltByBit</bold> resource page(s).</gray>'><gradient:#3498DB:#9B59B6><bold>Commands</bold></gradient></hover>",
                "<hover:show_text:'<gray>Open</gray> <gradient:#3498DB:#9B59B6><bold>NexusCore</bold></gradient> <gray>main menu GUI.</gray>'> <click:run_command:'/nexus'><dark_aqua>/nexus</dark_aqua></click></hover> <gray>— Main Menu</gray>",
                "<hover:show_text:'<gray>Function:</gray> <gradient:#3498DB:#9B59B6><bold>Reload NexusCore</bold></gradient>\n\n<gray>Reload all files:</gray>\n<blue>•</blue> <gray>config.yml</gray>\n<blue>•</blue> <gray>scoreboard.yml</gray>\n<blue>•</blue> <gray>tablist.yml</gray>'> <click:run_command:'/nexus reload'><dark_aqua>/nexus reload</dark_aqua></click></hover> <gray>— Reload all files</gray>",
                "<hover:show_text:'<gray>View the current running build release</gray>\n<gray>for</gray> <gradient:#3498DB:#9B59B6><bold>NexusCore</bold></gradient> <gray>on your server.</gray>'> <click:run_command:'/nexus version'><dark_aqua>/nexus version</dark_aqua></click></hover> <gray>— Version check</gray>",
                "",
                " <dark_aqua>/tp & /tphere</dark_aqua> <gray>— Teleport utilities</gray>",
                "  <click:suggest_command:'/tp <player>'><hover:show_text:'<green>Suggest /tp</green>'><dark_aqua>[Try /tp]</dark_aqua></hover></click> " +
                        "  <click:suggest_command:'/tphere <player>'><hover:show_text:'<green>Suggest /tphere</green>'><dark_aqua>[Try /tphere]</dark_aqua></hover></click>",
                "",
                navRow("help", "perms", "links")
        )));
        return Book.book(
                title("<gray>Commands</gray>"),
                author(plugin),
                pages
        );
    }
    public static Book permissions(JavaPlugin plugin) {
        List<Component> pages = new ArrayList<>();
        pages.add(page(String.join("\n",
                "<gradient:#3498DB:#9B59B6><bold>Permissions</bold></gradient>",
                " <yellow>nexuscore.use</yellow> <gray>— /nexus (default: true)</gray>",
                " <yellow>nexuscore.reload</yellow> <gray>— /nexus reload (default: op)</gray>",
                " <yellow>nexuscore.version</yellow> <gray>— /nexus version (default: true)</gray>",
                " <yellow>nexuscore.tp</yellow> <gray>— /tp <player> (default: true)</gray>",
                " <yellow>nexuscore.tphere</yellow> <gray>— /tphere <player> (default: true)</gray>",
                " <yellow>nexuscore.tp.other</yellow> <gray>— /tp <a> <b> (default: op)</gray>",
                "",
                navRow("help", "commands", "links")
        )));
        return Book.book(
                title("<gray>Permissions</gray>"),
                author(plugin),
                pages
        );
    }
    public static Book links(JavaPlugin plugin, String spigotUrl, String bbbUrl, String discordUrl) {
        List<Component> pages = new ArrayList<>();
        pages.add(page(String.join("\n",
                "<gradient:#3498DB:#9B59B6><bold>Links</bold></gradient>",
                linkRow("SpigotMC", spigotUrl),
                linkRow("BuiltByBit", bbbUrl),
                linkRow("Discord", discordUrl),
                "",
                navRow("help", "commands", "perms")
        )));
        return Book.book(
                title("<gray>Links</gray>"),
                author(plugin),
                pages
        );
    }
    private static String linkRow(String label, String url) {
        if (url == null || url.isBlank()) {
            return "<gray>" + label + ":</gray> <dark_gray>—</dark_gray>";
        }
        return " <click:open_url:'" + url + "'>" +
                "<hover:show_text:'<green>Open " + label + "</green>'>" +
                "<gradient:#3498DB:#9B59B6>[" + label + "]</gradient></hover></click>";
    }
    private static String navRow(String backOrHome, String next, String alt) {
        List<String> parts = new ArrayList<>();
        if (backOrHome != null) {
            parts.add(btn("◀ " + pretty(backOrHome), "/nexushelp " + backOrHome));
        } else {
            parts.add("<gray> </gray>");
        }
        parts.add(btn("Home", "/nexushelp help"));
        if (next != null) {
            parts.add(btn(pretty(next) + " ▶", "/nexushelp " + next));
        } else if (alt != null) {
            parts.add(btn(pretty(alt), "/nexushelp " + alt));
        }
        return String.join("  ", parts);
    }
    private static String btn(String label, String command) {
        return "<click:run_command:'" + command + "'>" +
                "<hover:show_text:'<green>" + PlainTextComponentSerializer.plainText().serialize(MM.deserialize(label)) + "</green>'>" +
                "<dark_aqua>[" + label + "]</dark_aqua></hover></click>";
    }
    private static String pretty(String key) {
        return switch (key.toLowerCase()) {
            case "help" -> "Help";
            case "commands" -> "Commands";
            case "perms" -> "Permissions";
            case "links" -> "Links";
            default -> key;
        };
    }
    public static ItemStack createPhysicalBook(JavaPlugin plugin, Player viewer) {
        return createPhysicalBookFrom(plugin, coreOverview(plugin));
    }
    public static ItemStack createPhysicalBookFrom(JavaPlugin plugin, net.kyori.adventure.inventory.Book book) {
        ItemStack stack = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) stack.getItemMeta();
        try {
            meta.title(book.title());
            meta.author(book.author());
            meta.addPages(book.pages().toArray(Component[]::new));
        } catch (Throwable t) {
            PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();
            meta.setTitle(plain.serialize(book.title()));
            meta.setAuthor(plain.serialize(book.author()));
            for (Component page : book.pages()) {
                meta.addPage(plain.serialize(page));
            }
        }
        try {
            meta.displayName(book.title());
        } catch (Throwable ignored) {
        }
        try {
            List<Component> lore = new ArrayList<>();
            lore.add(MM.deserialize("<gray>Admin Guide • </gray><gradient:#3498DB:#9B59B6><b>NexusCore</b></gradient>"));
            lore.add(MM.deserialize("<dark_gray>»</dark_gray> <white>Click to open the guide</white>"));
            lore.add(MM.deserialize("<dark_gray>»</dark_gray> <white>Drop/move = <red>burn & vanish</red></white>"));
            lore.add(MM.deserialize("<dark_gray>—</dark_gray> <gray>v</gray><white>" + plugin.getDescription().getVersion() + "</white>"));
            meta.lore(lore);
        } catch (Throwable t) {
            List<String> lore = new ArrayList<>();
            lore.add("Admin Guide • NexusCore");
            lore.add("» Click to open the guide");
            lore.add("» Drop/move = burn & vanish");
            lore.add("— v" + plugin.getDescription().getVersion());
            meta.setLore(lore);
        }
        meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_UNBREAKABLE
        );
        try { meta.addItemFlags(ItemFlag.valueOf("HIDE_ADDITIONAL_TOOLTIP")); } catch (Throwable ignored) {}
        try { meta.addItemFlags(ItemFlag.valueOf("HIDE_DYE")); } catch (Throwable ignored) {}
        try { meta.addItemFlags(ItemFlag.valueOf("HIDE_ARMOR_TRIM")); } catch (Throwable ignored) {}
        try { meta.addItemFlags(ItemFlag.valueOf("HIDE_ITEM_SPECIFICS")); } catch (Throwable ignored) {}
        boolean glintSet = false;
        try {
            meta.setEnchantmentGlintOverride(true);
            glintSet = true;
        } catch (Throwable ignored) {}
        if (!glintSet) {
            try {
                meta.addEnchant(Enchantment.SHARPNESS, 1, true);
            } catch (Throwable ignored) {
                try { meta.addEnchant(Enchantment.MENDING, 1, true); } catch (Throwable ignored2) {}
            }
        }
        stack.setItemMeta(meta);
        return stack;
    }
}