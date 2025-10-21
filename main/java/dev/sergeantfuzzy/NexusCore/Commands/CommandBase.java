package dev.sergeantfuzzy.NexusCore.Commands;

import dev.sergeantfuzzy.NexusCore.Commands.Admin.AdminCommandReload;
import dev.sergeantfuzzy.NexusCore.Commands.Admin.Util.CommandVersion;
import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CommandBase implements TabExecutor {
    private final JavaPlugin plugin;
    private final AdminCommandReload reloadCmd;
    private final CommandVersion versionCmd;
    private final String permission;
    private final String usage;
    private final boolean allowConsole;
    public CommandBase(JavaPlugin plugin, AdminCommandReload reloadCmd, CommandVersion versionCmd) {
        this(plugin, reloadCmd, versionCmd, null, "/nexus reload|version", true);
    }
    public CommandBase(JavaPlugin plugin, AdminCommandReload reloadCmd, CommandVersion versionCmd,
                       String permission, String usage, boolean allowConsole) {
        this.plugin = plugin;
        this.reloadCmd = reloadCmd;
        this.versionCmd = versionCmd;
        this.permission = permission;
        this.usage = usage;
        this.allowConsole = allowConsole;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!allowConsole && !(sender instanceof Player)) {
            i18n.sendMM(sender, "<red>This command cannot be used from console.</red>");
            return true;
        }
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            i18n.sendMM(sender, "<red>You do not have permission to use this command.</red>");
            return true;
        }
        if (args.length == 0) {
            if (sender instanceof Player player) {
                dev.sergeantfuzzy.NexusCore.GUI.NexusCoreMenu.open(plugin, player);
                i18n.sendMM(
                        player,
                        Msg.CHAT_PREFIX_MM +
                                "<gray>Opening </gray><gradient:#3498DB:#9B59B6><bold>Nexus Core</bold></gradient><gray> menu...</gray>"
                );
                return true;
            } else {
                sendUsage(sender);
                return true;
            }
        }
        String sub = args[0] == null ? "" : args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                String[] forwarded = slice(args, 1);
                return reloadCmd.execute(sender, label, forwarded);
            }
            case "version" -> {
                if (!sender.hasPermission("nexuscore.version")) {
                    i18n.sendMM(sender, "<red>You do not have permission to use this command.</red>");
                    return true;
                }
                return versionCmd.execute(sender, slice(args, 1));
            }
            default -> {
                sendUsage(sender);
                return true;
            }
        }
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            var base = new ArrayList<String>();
            base.add("reload");
            if (sender.hasPermission("nexuscore.version")) base.add("version");
            String prefix = args[0] == null ? "" : args[0].toLowerCase();
            if (prefix.isEmpty()) return base;
            List<String> out = new ArrayList<>();
            for (String s : base) if (s.startsWith(prefix)) out.add(s);
            return out;
        }
        if (args.length >= 2) {
            String sub = args[0] == null ? "" : args[0].toLowerCase();
            if ("reload".equals(sub)) {
                String[] forwarded = slice(args, 1);
                List<String> res = reloadCmd.tabComplete(sender, alias, forwarded);
                return res != null ? res : Collections.emptyList();
            }
            if ("version".equals(sub)) {
            }
        }
        return Collections.emptyList();
    }
    private void sendUsage(CommandSender sender) {
        i18n.sendUsage(sender, usage);
    }
    private static String[] slice(String[] in, int from) {
        if (from >= in.length) return new String[0];
        String[] out = new String[in.length - from];
        System.arraycopy(in, from, out, 0, out.length);
        return out;
    }
}