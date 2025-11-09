package dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Movement;

import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Util.GameplayCommandUtil.*;

public final class JumpCommand implements CommandExecutor, TabCompleter {
    private static final String PERM_SELF   = "nexuscore.jump";
    private static final String PERM_OTHERS = "nexuscore.jump.others";
    public JumpCommand(JavaPlugin plugin) {
        // (reserved for future: config hooks, cooldowns, etc.)
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!sender.hasPermission(PERM_SELF) || !ensurePlayerSender(sender)) return true;
            Player p = (Player) sender;
            if (doJump(p)) {
                sendPrefixedMM(sender, "<green>Jumped to your crosshair target.</green> " +
                        choiceBar("/jump", "Jump", "—", "Jump others"));
            } else {
                sendPrefixedMM(sender, "<red>No safe block found in sight.</red>");
            }
            return true;
        }
        if (!sender.hasPermission(PERM_OTHERS)) {
            i18n.sendMM(sender, Msg.CHAT_PREFIX_MM + "<red>Only admins may make others jump.</red>");
            return true;
        }
        Optional<Player> opt = findPlayer(args[0]);
        if (opt.isEmpty()) {
            sendPrefixedMM(sender, "<red>Player <b><name></b> is not online.</red>",
                    Placeholder.unparsed("name", args[0]));
            return true;
        }
        Player t = opt.get();
        if (doJump(t)) {
            sendPrefixedMM(sender, "<green>Jumped <b><name></b> to their target block.</green>",
                    Placeholder.unparsed("name", t.getName()));
        } else {
            sendPrefixedMM(sender, "<red>No safe block for <b><name></b>.</red>",
                    Placeholder.unparsed("name", t.getName()));
        }
        return true;
    }
    private boolean doJump(Player p) {
        RayTraceResult rt = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getLocation().getDirection(), 100);
        if (rt == null || rt.getHitBlock() == null) return false;
        Location hit = rt.getHitBlock().getLocation().add(0.5, 1, 0.5);
        if (!hit.getBlock().isEmpty() || !hit.clone().add(0,1,0).getBlock().isEmpty()) return false;
        if (hit.getBlock().isLiquid()) return false;
        p.teleport(hit);
        p.setVelocity(new Vector(0, 0.6, 0));
        p.getWorld().playSound(hit, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
        p.getWorld().spawnParticle(Particle.PORTAL, hit, 30, 0.3, 0.5, 0.3, 0.05);
        return true;
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
