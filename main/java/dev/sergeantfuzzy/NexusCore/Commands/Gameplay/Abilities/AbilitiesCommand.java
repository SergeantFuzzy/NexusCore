package dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Abilities;

import dev.sergeantfuzzy.NexusCore.Abilities.Ability;
import dev.sergeantfuzzy.NexusCore.Abilities.AbilityManager;
import dev.sergeantfuzzy.NexusCore.GUI.Abilities.AbilitiesMenu;
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
import java.util.Locale;
import java.util.Objects;

public final class AbilitiesCommand implements TabExecutor {
    private static final String BASE_PERMISSION = "nexuscore.abilities";
    private static final String GUI_PERMISSION = "nexuscore.abilities.gui";
    private final JavaPlugin plugin;

    public AbilitiesCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(BASE_PERMISSION)) {
            i18n.sendMM(sender, "<red>You do not have permission to use this command.</red>");
            return true;
        }
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sendUsage(sender);
                return true;
            }
            openMenu(player);
            return true;
        }
        String sub = args[0] == null ? "" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "menu", "gui" -> {
                if (!(sender instanceof Player player)) {
                    i18n.sendMM(sender, "<yellow>Only players can open the abilities menu.</yellow>");
                    return true;
                }
                openMenu(player);
                return true;
            }
            case "list" -> {
                sendAbilityList(sender);
                return true;
            }
            case "info" -> {
                if (args.length < 2) {
                    i18n.sendUsage(sender, "/abilities info <ability>");
                    return true;
                }
                Ability ability = Ability.fromInput(args[1]);
                if (ability == null) {
                    i18n.sendMM(sender, "<red>Unknown ability:</red> <gray>" + args[1] + "</gray>");
                    return true;
                }
                sendAbilityInfo(sender, ability);
                return true;
            }
            case "use" -> {
                if (!(sender instanceof Player player)) {
                    i18n.sendMM(sender, "<yellow>Only players can activate abilities.</yellow>");
                    return true;
                }
                if (args.length < 2) {
                    i18n.sendUsage(sender, "/abilities use <ability>");
                    return true;
                }
                Ability ability = Ability.fromInput(args[1]);
                if (ability == null) {
                    i18n.sendMM(sender, "<red>Unknown ability:</red> <gray>" + args[1] + "</gray>");
                    return true;
                }
                if (!player.hasPermission(ability.permissionNode())) {
                    i18n.sendMM(player, "<red>You lack permission:</red> <gray>" + ability.permissionNode() + "</gray>");
                    return true;
                }
                AbilityManager manager = AbilityManager.instance();
                AbilityManager.AbilityResult result = manager.activate(player, ability);
                switch (result.status()) {
                    case SUCCESS -> i18n.sendMM(
                            player,
                            Msg.CHAT_PREFIX_MM + "<green>Activated ability:</green> <yellow>" + ability.displayName() + "</yellow>"
                    );
                    case DISABLED_GLOBAL -> i18n.sendMM(player, "<red>Abilities are currently disabled server-wide.</red>");
                    case DISABLED_ABILITY -> i18n.sendMM(player, "<yellow>" + ability.displayName() + " is temporarily disabled.</yellow>");
                    case ON_COOLDOWN -> i18n.sendMM(
                            player,
                            "<red>" + ability.displayName() + "</red> <gray>is on cooldown for</gray> <yellow>" +
                                    result.secondsRemaining() + "s</yellow>."
                    );
                }
                return true;
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
            List<String> sub = new ArrayList<>();
            Collections.addAll(sub, "menu", "gui", "list", "info", "use");
            return matchPrefix(sub, args[0]);
        }
        if (args.length == 2) {
            String sub = args[0] == null ? "" : args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("info") || sub.equals("use")) {
                return matchPrefix(abilityIds(), args[1]);
            }
        }
        return Collections.emptyList();
    }

    private void sendUsage(CommandSender sender) {
        i18n.sendUsage(sender, "/abilities [menu|list|info <ability>|use <ability>]");
    }

    private void sendAbilityList(CommandSender sender) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gradient:#3498DB:#9B59B6><bold>Abilities</bold></gradient><gray>:</gray> ");
        boolean first = true;
        for (Ability ability : Ability.values()) {
            if (!first) sb.append("<gray>, </gray>");
            sb.append("<yellow>").append(ability.displayName()).append("</yellow>");
            first = false;
        }
        i18n.sendMM(sender, sb.toString());
    }

    private void sendAbilityInfo(CommandSender sender, Ability ability) {
        AbilityManager manager = AbilityManager.instance();
        long cd = manager.configuredCooldown(ability);
        StringBuilder lore = new StringBuilder();
        lore.append("<gradient:#3498DB:#9B59B6><bold>").append(ability.displayName()).append("</bold></gradient>\n");
        for (String line : ability.description()) {
            lore.append("<gray>").append(line.replace("§7", "")).append("</gray>\n");
        }
        lore.append("<gray>Cooldown:</gray> <yellow>").append(cd).append("s</yellow>\n");
        lore.append("<gray>Permission:</gray> <yellow>").append(ability.permissionNode()).append("</yellow>");
        i18n.sendMM(sender, lore.toString());
    }

    private void openMenu(Player player) {
        if (!player.hasPermission(GUI_PERMISSION)) {
            i18n.sendMM(player, "<red>You do not have permission to open the abilities menu.</red>");
            return;
        }
        AbilitiesMenu.open(plugin, player);
        i18n.sendMM(
                player,
                Msg.CHAT_PREFIX_MM +
                        "<gray>Opening </gray><gradient:#F1C40F:#E67E22><bold>Abilities</bold></gradient><gray> menu...</gray>"
        );
    }

    private static List<String> abilityIds() {
        List<String> list = new ArrayList<>();
        for (Ability ability : Ability.values()) {
            list.add(ability.id());
        }
        return list;
    }

    private static List<String> matchPrefix(List<String> values, String prefix) {
        if (prefix == null || prefix.isEmpty()) return values;
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> match = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(lower)) {
                match.add(value);
            }
        }
        return match;
    }
}
