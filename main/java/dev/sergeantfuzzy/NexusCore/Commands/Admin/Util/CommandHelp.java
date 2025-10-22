package dev.sergeantfuzzy.NexusCore.Commands.Admin.Util;

import dev.sergeantfuzzy.NexusCore.UI.Help.HelpBookUI;
import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CommandHelp implements TabExecutor {
    private final JavaPlugin plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final String SPIGOT_URL;
    private final String BBB_URL;
    private final String DISCORD_URL;
    public CommandHelp(JavaPlugin plugin, String spigotUrl, String bbbUrl, String discordUrl) {
        this.plugin = plugin;
        this.SPIGOT_URL = spigotUrl;
        this.BBB_URL = bbbUrl;
        this.DISCORD_URL = discordUrl;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String section = (args.length == 0) ? "help" : args[0].toLowerCase(Locale.ROOT);
        if (sender instanceof Player player) {
            Book book = switch (section) {
                case "help", "home" -> HelpBookUI.coreOverview(plugin);
                case "commands" -> HelpBookUI.commands(plugin);
                case "perms", "permissions" -> HelpBookUI.permissions(plugin);
                case "links" -> HelpBookUI.links(plugin, SPIGOT_URL, BBB_URL, DISCORD_URL);
                default -> HelpBookUI.coreOverview(plugin);
            };
            player.openBook(book);
            return true;
        }
        consoleFallback(sender, section);
        return true;
    }
    private void consoleFallback(CommandSender console, String section) {
        console.sendMessage("");
        i18n.sendMM(console, Msg.CHAT_PREFIX_MM + "<white><bold>NexusCore Help (Console)</bold></white>");
        switch (section) {
            case "commands" -> {
                i18n.sendMM(console, " <yellow>/nexus</yellow> <gray>— Open main GUI</gray>");
                i18n.sendMM(console, " <yellow>/nexus reload</yellow> <gray>— Reload configs/messages</gray>");
                i18n.sendMM(console, " <yellow>/nexus version</yellow> <gray>— Plugin version/build</gray>");
                i18n.sendMM(console, " <yellow>/tp & /tphere</yellow> <gray>— Teleport utilities</gray>");
            }
            case "perms", "permissions" -> {
                i18n.sendMM(console, " <yellow>nexuscore.use</yellow> <gray>— /nexus (default: true)</gray>");
                i18n.sendMM(console, " <yellow>nexuscore.reload</yellow> <gray>— /nexus reload (default: op)</gray>");
                i18n.sendMM(console, " <yellow>nexuscore.version</yellow> <gray>— /nexus version (default: true)</gray>");
                i18n.sendMM(console, " <yellow>nexuscore.tp</yellow> <gray>— /tp <player> (default: true)</gray>");
                i18n.sendMM(console, " <yellow>nexuscore.tphere</yellow> <gray>— /tphere <player> (default: true)</gray>");
                i18n.sendMM(console, " <yellow>nexuscore.tp.other</yellow> <gray>— /tp <a> <b> (default: op)</gray>");
            }
            case "links" -> {
                i18n.sendMM(console, " SpigotMC: <blue>" + SPIGOT_URL + "</blue>");
                i18n.sendMM(console, " BBB:      <blue>" + BBB_URL + "</blue>");
                i18n.sendMM(console, " Discord:  <blue>" + DISCORD_URL + "</blue>");
            }
            default -> {
                i18n.sendMM(console, " Use: <yellow>/nexushelp</yellow> [<gray>commands</gray>|<gray>perms</gray>|<gray>links</gray>]");
            }
        }
        i18n.sendMM(console, "");
    }
    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> opts = List.of("help", "commands", "perms", "links");
            String p = args[0].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            for (String s : opts) if (s.startsWith(p)) out.add(s);
            return out;
        }
        return List.of();
    }
}