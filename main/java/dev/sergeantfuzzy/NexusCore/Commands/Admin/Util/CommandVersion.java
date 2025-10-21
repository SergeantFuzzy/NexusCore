package dev.sergeantfuzzy.NexusCore.Commands.Admin.Util;

import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class CommandVersion {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String DISCORD_URL = "https://discord.com/users/193943556459724800";
    private static final String SPIGOT_URL  = "https://www.spigotmc.org/resources/";
    private static final String BBB_URL     = "https://builtbybit.com/resources/";
    private final JavaPlugin plugin;
    public CommandVersion(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public boolean execute(CommandSender sender, String[] args) {
        final String pluginName = plugin.getDescription().getName();
        final String version    = plugin.getDescription().getVersion();
        final String authors    = String.join(", ", plugin.getDescription().getAuthors());
        if (sender instanceof ConsoleCommandSender) {
            sendConsole(sender, pluginName, version, authors);
        } else {
            sendPlayer(sender, pluginName, version, authors);
        }
        return true;
    }
    private void sendPlayer(CommandSender sender, String pluginName, String version, String authors) {
        final String pluginVer  = plugin.getDescription().getVersion();
        i18n.sendMM(sender, "");
        i18n.sendMM(sender, MiniMessage.miniMessage().serialize(Msg.chatRule()));
        i18n.sendMM(sender, "<gradient:#3498DB:#9B59B6><bold>" + pluginName + "</bold></gradient>");
        i18n.sendMM(sender, "<gray>Author:</gray> <white>" + authors + "</white>");
        i18n.sendMM(sender, "<gray>Version:</gray> <white>" + pluginVer + "</white>");
        i18n.sendMM(sender,
                "<gray>Contact:</gray> " +
                        "<click:open_url:'" + DISCORD_URL + "'>" +
                        "<hover:show_text:'<green>SergeantFuzzy</green><gray> — DM for bugs, issues, update requests.</gray>'>" +
                        "<gradient:#5865F2:#7289DA><bold>[Discord]</bold></gradient></hover></click>"
        );
        i18n.sendMM(sender, "");
        i18n.sendMM(sender,
                "<click:open_url:'" + BBB_URL + "'>" +
                        "<hover:show_text:'<gray>Open BuiltByBit resource page</gray>'>" +
                        "<gradient:#F39C12:#E67E22><bold>[BuiltByBit]</bold></gradient></hover></click> " +
                        "<click:open_url:'" + SPIGOT_URL + "'>" +
                        "<hover:show_text:'<gray>Open SpigotMC resource page</gray>'>" +
                        "<gradient:#F39C12:#D35400><bold>[SpigotMC]</bold></gradient></hover></click>"
        );
        i18n.sendMM(sender, MiniMessage.miniMessage().serialize(Msg.chatRule()));
    }
    private void sendConsole(CommandSender sender, String pluginName, String version, String authors) {
        final String pluginVer  = plugin.getDescription().getVersion();
        final Component rule = Msg.consoleRule();
        final MiniMessage mm = MiniMessage.miniMessage();
        sender.sendMessage(Component.empty());
        sender.sendMessage(rule);
        sender.sendMessage(mm.deserialize("<gradient:#3498DB:#9B59B6><bold>" + pluginName + "</bold></gradient>"));
        sender.sendMessage(mm.deserialize("<gray>Author:</gray> <white>" + authors + "</white>"));
        sender.sendMessage(mm.deserialize("<gray>Version:</gray> <white>" + pluginVer + "</white>"));
        sender.sendMessage(mm.deserialize("<gray>Contact:</gray> <white>Discord (SergeantFuzzy)</white>"));
        sender.sendMessage(mm.deserialize("<gray>Link:</gray> <white>" + DISCORD_URL + "</white>"));
        sender.sendMessage(Component.empty());
        sender.sendMessage(mm.deserialize("<gray>BuiltByBit:</gray> <white>" + BBB_URL + "</white>"));
        sender.sendMessage(mm.deserialize("<gray>SpigotMC:</gray>  <white>" + SPIGOT_URL + "</white>"));
        sender.sendMessage(rule);
    }
}