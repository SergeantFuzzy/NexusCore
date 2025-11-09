package dev.sergeantfuzzy.NexusCore.Commands;

import dev.sergeantfuzzy.NexusCore.Commands.Admin.AdminCommandReload;
import dev.sergeantfuzzy.NexusCore.Commands.Admin.Util.CommandHelp;
import dev.sergeantfuzzy.NexusCore.Commands.Admin.Util.CommandVersion;
import dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Abilities.AbilitiesCommand;
import dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Combat.HealCommand;
import dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Combat.HealthCommand;
import dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Movement.BackCommand;
import dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Movement.FlyCommand;
import dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Movement.JumpCommand;
import dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Movement.RandomTPCommand;
import dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Movement.TPCommand;
import dev.sergeantfuzzy.NexusCore.Commands.Gameplay.Survival.FeedCommand;
import dev.sergeantfuzzy.NexusCore.Commands.Gameplay.World.SpawnCommand;
import dev.sergeantfuzzy.NexusCore.Listeners.Commands.HelpOverrideListener;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Locale;

import static dev.sergeantfuzzy.NexusCore.Commands.EssentialsTabRegistry.*;

public final class CommandRegistrar {
    private CommandRegistrar() {}
    public static void registerAll(JavaPlugin plugin, AdminCommandReload reloadCmd) {
        EssentialsDynamicCompleter globalCompleter = new EssentialsDynamicCompleter();
        CommandVersion versionCmd = new CommandVersion(plugin);
        {
            CommandBase base = new CommandBase(plugin, reloadCmd, versionCmd);
            register(plugin, "nexus", "NexusCore base command", List.of("nc", "ncore"), base, globalCompleter);
            EssentialsTabRegistry.register("nexus", (sender, alias, args) -> {
                final String PERM_VER = "nexuscore.version";
                if (args.length == 0) {
                    var opts = new java.util.ArrayList<String>();
                    opts.add("reload");
                    if (sender.hasPermission(PERM_VER)) opts.add("version");
                    return opts;
                }
                if (args.length == 1) {
                    var opts = new java.util.ArrayList<String>();
                    opts.add("reload");
                    if (sender.hasPermission(PERM_VER)) opts.add("version");
                    return matchPrefix(opts, args[0]);
                }
                String sub = args[0] == null ? "" : args[0].toLowerCase(Locale.ROOT);
                if ("reload".equals(sub)) {
                    var forwarded = slice(args, 1);
                    var res = reloadCmd.tabComplete(sender, alias, forwarded);
                    return (res == null) ? List.of() : res;
                }
                if ("version".equals(sub)) {
                    return List.of();
                }
                return List.of();
            });
            EssentialsTabRegistry.register("nc", EssentialsTabRegistry.get("nexus"));
            EssentialsTabRegistry.register("ncore", EssentialsTabRegistry.get("nexus"));
        }
        {
            final String SPIGOT_URL = "https://www.spigotmc.org/resources/129669/";
            final String BBB_URL    = "https://builtbybit.com/resources/80896/";
            final String DISCORD    = "https://discord.com/users/193943556459724800";
            CommandHelp helpExec = new CommandHelp(plugin, SPIGOT_URL, BBB_URL, DISCORD);
            register(plugin,
                    "nexushelp",
                    "Open NexusCore Help (Book UI)",
                    List.of("nhelp"),
                    helpExec);
            EssentialsTabRegistry.register("nexushelp", (sender, alias, args) -> {
                if (args.length != 1) return List.of();
                var opts = List.of("help", "commands", "perms", "links");
                return matchPrefix(opts, args[0]);
            });
            EssentialsTabRegistry.register("nhelp", EssentialsTabRegistry.get("nexushelp"));
            plugin.getServer().getPluginManager().registerEvents(new HelpOverrideListener(plugin), plugin);
        }
        {
            AbilitiesCommand abilities = new AbilitiesCommand(plugin);
            register(
                    plugin,
                    "abilities",
                    "Open the abilities menu or activate a Nexus ability",
                    List.of("ability", "skills"),
                    abilities,
                    abilities
            );
        }
        registerEssentials(plugin, globalCompleter);
    }
    public static void register(JavaPlugin plugin) {
        registerAll(plugin, new AdminCommandReload(plugin));
    }
    private static void registerEssentials(JavaPlugin plugin, TabCompleter globalCompleter) {
        {
            FeedCommand exec = new FeedCommand(plugin);
            register(plugin, "feed", "Fill hunger/saturation", List.of(), exec, globalCompleter);
            EssentialsTabRegistry.register("feed", (sender, alias, args) -> {
                final String PERM_OTHERS = "nexuscore.feed.others";
                if (args.length == 0) return List.of("status");
                if (args.length == 1) {
                    var opts = new java.util.ArrayList<String>();
                    opts.add("status");
                    if (sender.hasPermission(PERM_OTHERS)) opts.addAll(onlinePlayerNames());
                    return matchPrefix(opts, args[0]);
                }
                if (args.length == 2 && "status".equalsIgnoreCase(args[0]) && sender.hasPermission(PERM_OTHERS)) {
                    return matchPrefix(onlinePlayerNames(), args[1]);
                }
                return List.of();
            });
        }
        {
            HealCommand exec = new HealCommand(plugin);
            register(plugin, "heal", "Restore health", List.of(), exec, globalCompleter);
            EssentialsTabRegistry.register("heal", (sender, alias, args) -> {
                String permOthers = "nexuscore.heal.others";
                if (args.length == 1 && sender.hasPermission(permOthers)) {
                    return matchPrefix(onlinePlayerNames(), args[0]);
                }
                return List.of();
            });
        }
        {
            HealthCommand exec = new HealthCommand(plugin);
            register(plugin, "health", "Show health info", List.of("hp"), exec, globalCompleter);
            EssentialsTabRegistry.register("health", (sender, alias, args) -> {
                String permOthers = "nexuscore.health.others";
                if (args.length == 1 && sender.hasPermission(permOthers)) {
                    return matchPrefix(onlinePlayerNames(), args[0]);
                }
                return List.of();
            });
            EssentialsTabRegistry.register("hp", EssentialsTabRegistry.get("health"));
        }
        {
            FlyCommand exec = new FlyCommand(plugin);
            register(plugin, "fly", "Toggle flight", List.of(), exec, globalCompleter);
            EssentialsTabRegistry.register("fly", (sender, alias, args) -> {
                String permOthers = "nexuscore.fly.others";
                if (args.length == 1) {
                    if (sender.hasPermission(permOthers)) {
                        return matchPrefix(onlinePlayerNames(), args[0]);
                    }
                    return matchPrefix(boolYesNo(), args[0]);
                }
                if (args.length == 2 && sender.hasPermission(permOthers)) {
                    return matchPrefix(boolYesNo(), args[1]);
                }
                return List.of();
            });
        }
        {
            JumpCommand exec = new JumpCommand(plugin);
            register(plugin, "jump", "Small vertical boost", List.of(), exec, globalCompleter);
            EssentialsTabRegistry.register("jump", (sender, alias, args) -> List.of());
        }
        {
            RandomTPCommand exec = new RandomTPCommand(plugin);
            register(plugin, "rtp", "Random teleport", List.of("randomtp"), exec, globalCompleter);
            EssentialsTabRegistry.register("rtp", (sender, alias, args) -> {
                if (args.length == 0) return List.of();
                final String PERM_OTHERS = "nexuscore.rtp.others";
                if (args.length == 1 && sender.hasPermission(PERM_OTHERS)) {
                    return EssentialsTabRegistry.matchPrefix(EssentialsTabRegistry.onlinePlayerNames(), args[0]);
                }
                return List.of();
            });
            EssentialsTabRegistry.register("randomtp", EssentialsTabRegistry.get("rtp"));
        }
        {
            BackCommand exec = new BackCommand(plugin);
            plugin.getServer().getPluginManager().registerEvents(exec, plugin);
            register(plugin, "back", "Return to a previous location", List.of(), exec, globalCompleter);
            EssentialsTabRegistry.register("back", (sender, alias, args) -> {
                final String PERM_OTHERS = "nexuscore.back.others";
                if (args.length == 1 && sender.hasPermission(PERM_OTHERS)) {
                    return matchPrefix(onlinePlayerNames(), args[0]);
                }
                return List.of();
            });
        }
        {
            SpawnCommand exec = new SpawnCommand(plugin);
            register(
                    plugin,
                    "spawn",
                    "Teleport to spawn; set/reset spawn; send a player to spawn",
                    List.of(),
                    exec,
                    globalCompleter
            );
            EssentialsTabRegistry.register("spawn", (sender, alias, args) -> {
                String PERM_SET   = SpawnCommand.PERM_SET;
                String PERM_RESET = SpawnCommand.PERM_RESET;
                String PERM_SEND  = SpawnCommand.PERM_SEND;
                if (args.length == 0) {
                    return List.of();
                }
                if (args.length == 1) {
                    java.util.List<String> opts = new java.util.ArrayList<>();
                    if (sender.hasPermission(PERM_SET))   opts.add("set");
                    if (sender.hasPermission(PERM_RESET)) opts.add("reset");
                    if (sender.hasPermission(PERM_SEND))  { opts.add("player"); opts.add("p"); }
                    return matchPrefix(opts, args[0]);
                }
                if (args.length == 2 && (args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("p"))) {
                    if (!sender.hasPermission(PERM_SEND)) return List.of();
                    return matchPrefix(onlinePlayerNames(), args[1]);
                }
                return List.of();
            });
        }
        {
            TPCommand exec = new TPCommand(plugin);
            register(
                    plugin,
                    "tp",
                    "Teleport to a player or move one player to another",
                    List.of("teleport"),
                    exec
            );
            register(
                    plugin,
                    "tphere",
                    "Teleport a player to yourself",
                    List.of("teleporthere"),
                    exec
            );
            EssentialsTabRegistry.register("tp", (sender, alias, args) -> {
                final String PERM_SELF   = "nexuscore.tp";
                final String PERM_HERE   = "nexuscore.tphere";
                final String PERM_OTHER  = "nexuscore.tp.other";
                if (args.length == 0) return List.of();
                var names = EssentialsTabRegistry.onlinePlayerNames();
                if (args.length == 1) {
                    boolean isPlayer = (sender instanceof org.bukkit.entity.Player);
                    java.util.ArrayList<String> base = new java.util.ArrayList<>();
                    if (sender.hasPermission(PERM_SELF)) {
                        base.addAll(names);
                    }
                    if (isPlayer && sender.hasPermission(PERM_HERE)) {
                        base.add("here");
                    }
                    if (sender.hasPermission(PERM_OTHER) && base.isEmpty()) {
                        base.addAll(names);
                    }
                    return EssentialsTabRegistry.matchPrefix(base, args[0]);
                }
                if (args.length == 2 && sender.hasPermission(PERM_OTHER)) {
                    return EssentialsTabRegistry.matchPrefix(names, args[1]);
                }
                return List.of();
            });
            EssentialsTabRegistry.register("teleport", EssentialsTabRegistry.get("tp"));
            EssentialsTabRegistry.register("tphere", (sender, alias, args) -> {
                final String PERM_HERE = "nexuscore.tphere";
                if (args.length != 1 || !sender.hasPermission(PERM_HERE)) return List.of();
                return EssentialsTabRegistry.matchPrefix(EssentialsTabRegistry.onlinePlayerNames(), args[0]);
            });
        }
    }
    private static void register(JavaPlugin plugin,
                                 String name,
                                 String description,
                                 List<String> aliases,
                                 TabExecutor executor) {
        PluginCommand pc = newPluginCommand(plugin, name);
        pc.setDescription(description);
        if (aliases != null && !aliases.isEmpty()) pc.setAliases(aliases);
        pc.setExecutor(executor);
        pc.setTabCompleter(new EssentialsDynamicCompleter());
        commandMap().register(plugin.getDescription().getName(), pc);
    }
    private static void register(JavaPlugin plugin,
                                 String name,
                                 String description,
                                 List<String> aliases,
                                 org.bukkit.command.CommandExecutor executor,
                                 org.bukkit.command.TabCompleter tabCompleter) {
        PluginCommand pc = newPluginCommand(plugin, name);
        pc.setDescription(description);
        if (aliases != null && !aliases.isEmpty()) pc.setAliases(aliases);
        pc.setExecutor(executor);
        if (tabCompleter != null) pc.setTabCompleter(tabCompleter);
        commandMap().register(plugin.getDescription().getName(), pc);
    }
    private static PluginCommand newPluginCommand(JavaPlugin plugin, String name) {
        try {
            Constructor<PluginCommand> c =
                    PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            return c.newInstance(name, plugin);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create PluginCommand for " + name, e);
        }
    }
    private static CommandMap commandMap() {
        try {
            Server server = Bukkit.getServer();
            try {
                var m = server.getClass().getDeclaredMethod("getCommandMap");
                m.setAccessible(true);
                return (CommandMap) m.invoke(server);
            } catch (NoSuchMethodException ignore) {
                var f = server.getClass().getDeclaredField("commandMap");
                f.setAccessible(true);
                return (CommandMap) f.get(server);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to access Bukkit CommandMap", e);
        }
    }
    private static String[] slice(String[] in, int from) {
        if (from >= in.length) return new String[0];
        String[] out = new String[in.length - from];
        System.arraycopy(in, from, out, 0, out.length);
        return out;
    }
}
