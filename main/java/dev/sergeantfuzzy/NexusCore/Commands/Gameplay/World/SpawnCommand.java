package dev.sergeantfuzzy.NexusCore.Commands.Gameplay.World;

import dev.sergeantfuzzy.NexusCore.Utilities.Msg;
import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class SpawnCommand implements TabExecutor {
    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration cfg;
    public static final String PERM_BASE  = "nexuscore.spawn";
    public static final String PERM_SET   = "nexuscore.spawn.set";
    public static final String PERM_RESET = "nexuscore.spawn.reset";
    public static final String PERM_SEND  = "nexuscore.spawn.player";
    public SpawnCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "spawn.yml");
        reload();
    }
    private void reload() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ignored) {}
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }
    private boolean hasSpawn() {
        return cfg.isString("world")
                && cfg.contains("x") && cfg.contains("y") && cfg.contains("z")
                && cfg.contains("yaw") && cfg.contains("pitch");
    }
    private Location loadSpawn() {
        if (!hasSpawn()) return null;
        World world = Bukkit.getWorld(Objects.requireNonNull(cfg.getString("world")));
        if (world == null) return null;
        double x = cfg.getDouble("x");
        double y = cfg.getDouble("y");
        double z = cfg.getDouble("z");
        float yaw = (float) cfg.getDouble("yaw");
        float pitch = (float) cfg.getDouble("pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }
    private void saveSpawn(Location loc) {
        cfg.set("world", Objects.requireNonNull(loc.getWorld()).getName());
        cfg.set("x", loc.getX());
        cfg.set("y", loc.getY());
        cfg.set("z", loc.getZ());
        cfg.set("yaw", loc.getYaw());
        cfg.set("pitch", loc.getPitch());
        try { cfg.save(file); } catch (IOException ignored) {}
    }
    private void resetSpawn() {
        cfg.set("world", null);
        cfg.set("x", null);
        cfg.set("y", null);
        cfg.set("z", null);
        cfg.set("yaw", null);
        cfg.set("pitch", null);
        try { cfg.save(file); } catch (IOException ignored) {}
    }
    private static void say(CommandSender to, String mmBody) {
        i18n.sendMM(to, Msg.CHAT_PREFIX_MM + mmBody);
    }
    private static boolean needPlayer(CommandSender s) {
        if (!(s instanceof Player)) {
            say(s, "<red>This command must be used by a player in-game.</red>");
            return true;
        }
        return false;
    }
    private static boolean checkPerm(CommandSender s, String perm) {
        if (!s.hasPermission(perm)) {
            say(s, "<red>You don't have permission to do that.</red>");
            return false;
        }
        return true;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!checkPerm(sender, PERM_BASE)) return true;
            if (needPlayer(sender)) return true;
            if (!hasSpawn()) {
                say(sender, "<red>Spawn point is not set.</red>");
                return true;
            }
            Location spawn = loadSpawn();
            if (spawn == null) {
                say(sender, "<red>Spawn world is unavailable or invalid.</red>");
                return true;
            }
            ((Player) sender).teleport(spawn);
            say(sender, "<green>Teleported to spawn.</green>");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "set" -> {
                if (!checkPerm(sender, PERM_SET)) return true;
                if (needPlayer(sender)) return true;
                Player p = (Player) sender;
                saveSpawn(p.getLocation());
                say(sender, "<green>Spawn point set to your current location.</green>");
                return true;
            }
            case "reset" -> {
                if (!checkPerm(sender, PERM_RESET)) return true;
                if (!hasSpawn()) {
                    say(sender, "<yellow>Spawn point has been already reset.</yellow>");
                    return true;
                }
                resetSpawn();
                say(sender, "<green>Spawn point has been reset.</green>");
                return true;
            }
            case "player", "p" -> {
                if (!checkPerm(sender, PERM_SEND)) return true;
                if (args.length < 2) {
                    say(sender, "<red>Usage:</red> <gray>/" + label + " player|p <player></gray>");
                    return true;
                }
                if (!hasSpawn()) {
                    say(sender, "<red>Cannot send player to spawn: spawn point is not set.</red>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !target.isOnline()) {
                    say(sender, "<red>That player is not online.</red>");
                    return true;
                }
                Location spawn = loadSpawn();
                if (spawn == null) {
                    say(sender, "<red>Spawn world is unavailable or invalid.</red>");
                    return true;
                }
                target.teleport(spawn);
                say(sender, "<green>Sent</green> <white>" + target.getName() + "</white> <green>to spawn.</green>");
                say(target, "<green>You have been sent to spawn.</green>");
                return true;
            }
            default -> {
                sendUsage(sender, label);
                return true;
            }
        }
    }
    private static void sendUsage(CommandSender s, String label) {
        say(s, "<aqua>/" + label + "</aqua> <gray>— Teleport to spawn (if set)</gray>");
        say(s, "<aqua>/" + label + " set</aqua> <gray>— Set your current location as spawn</gray>");
        say(s, "<aqua>/" + label + " reset</aqua> <gray>— Reset/clear the spawn</gray>");
        say(s, "<aqua>/" + label + " player|p <player></aqua> <gray>— Send an online player to spawn</gray>");
    }
    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String alias, String[] args) {
        if (args.length == 1) {
            List<String> opts = new ArrayList<>();
            if (s.hasPermission(PERM_SET))   opts.add("set");
            if (s.hasPermission(PERM_RESET)) opts.add("reset");
            if (s.hasPermission(PERM_SEND)) { opts.add("player"); opts.add("p"); }
            String pref = args[0].toLowerCase(Locale.ROOT);
            return opts.stream().filter(o -> o.startsWith(pref)).collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("p"))) {
            if (!s.hasPermission(PERM_SEND)) return Collections.emptyList();
            String pref = args[1].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(pref))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
