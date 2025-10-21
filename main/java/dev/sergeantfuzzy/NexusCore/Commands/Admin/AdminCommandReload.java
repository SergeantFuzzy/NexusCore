package dev.sergeantfuzzy.NexusCore.Commands.Admin;

import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public final class AdminCommandReload {
    public static final String PERM = "nexuscore.admin.reload";
    private final JavaPlugin plugin;
    public AdminCommandReload(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(PERM)) {
            i18n.sendMM(sender, "<red>You do not have permission to use this command.</red>");
            return true;
        }
        plugin.reloadConfig();
        i18n.sendMM(sender,
                "<gradient:#3498DB:#9B59B6><b>NexusCore</b></gradient> <gray>»</gray> " +
                        "<green>Configuration reloaded.</green> " +
                        "<gray>(</gray>" +
                        "<click:run_command:'/nexus reload'>" +
                        "<hover:show_text:'<yellow>Click to run <white>/nexus reload</white> again</yellow>'>" +
                        "<aqua><u>reload again</u></aqua>" +
                        "</hover></click>" +
                        "<gray>)</gray>"
        );
        return true;
    }
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return Collections.emptyList();
    }
}