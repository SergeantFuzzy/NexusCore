package dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Combat;

import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Util.GameplayCommandUtil.*;

public final class HealCommand implements CommandExecutor, TabCompleter {
    private static final String PERM_SELF   = "nexuscore.heal";
    private static final String PERM_OTHERS = "nexuscore.heal.others";
    public HealCommand(JavaPlugin plugin) {
        // (reserved for future: config hooks, cooldowns, etc.)
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!sender.hasPermission(PERM_SELF)) {
                i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>You lack permission.</red>");
                return true;
            }
            if (!ensurePlayerSender(sender)) return true;
            Player p = (Player) sender;
            heal(p);
            sendPrefixedMM(sender,
                    "<green>Healed yourself.</green>" + actionBar(
                            runAction("Heal Again", "/heal", "Instantly heal yourself"),
                            runAction("Health", "/health status", "Check your health meta"),
                            suggestAction("Heal Player", "/heal ", "Type a player to heal")
                    ));
            return true;
        }
        if (!sender.hasPermission(PERM_OTHERS)) {
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Only admins may heal others.</red>");
            return true;
        }
        Optional<Player> opt = findPlayer(args[0]);
        if (opt.isEmpty()) {
            sendPrefixedMM(sender, "<red>Player <b><name></b> is not online.</red>",
                    Placeholder.unparsed("name", args[0]));
            return true;
        }
        Player target = opt.get();
        heal(target);
        sendPrefixedMM(sender, "<green>Healed <b><name></b>.</green>" + actionBar(
                        runAction("Heal Again", "/heal " + target.getName(), "Top them off once more"),
                        runAction("Feed", "/feed " + target.getName(), "Feed them after the heal"),
                        suggestAction("Heal Player", "/heal ", "Type another player")
                ),
                Placeholder.unparsed("name", target.getName()));
        sendPrefixedMM(target, "<green>You were healed by <b><name></b>.</green>" + actionBar(
                        runAction("Health", "/health status", "Check your health info"),
                        runAction("Back", "/back", "Return to your previous spot")
                ),
                Placeholder.unparsed("name", sender.getName()));
        return true;
    }
    private void heal(Player p) {
        double max = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        p.setHealth(Math.min(max, max));
        p.setFireTicks(0);
        p.setFoodLevel(20);
        p.setSaturation(20f);
        p.setExhaustion(0f);
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission(PERM_OTHERS)) {
            Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
        }
        return out;
    }
}
