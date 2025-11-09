package dev.sergeantfuzzy.NexusCore;

import dev.sergeantfuzzy.NexusCore.Abilities.AbilityManager;
import dev.sergeantfuzzy.NexusCore.Commands.CommandRegistrar;
import dev.sergeantfuzzy.NexusCore.Listeners.AdminJoinListener;
import dev.sergeantfuzzy.NexusCore.Listeners.LeaveListener;
import dev.sergeantfuzzy.NexusCore.System.Systems;
import dev.sergeantfuzzy.NexusCore.Utilities.UpdateBootstrap;
import dev.sergeantfuzzy.NexusCore.Utilities.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;
import dev.sergeantfuzzy.NexusCore.Utilities.Msg;

/**
 * Main plugin class. Spigot loads this when the server starts NexusCore.
 */
public final class Main extends JavaPlugin {
    /** Keeps track of the update helper so we can stop it later. */
    private UpdateBootstrap updates;

    /** Allows other classes to use the update helper if they need it. */
    public UpdateBootstrap getUpdates() { return this.updates; }

    /** Spigot resource ID used to look up new versions. */
    private static final int SPIGOT_ID = 129669;

    /** BuiltByBit page where players can download updates. */
    private static final String BBB_URL   = "https://builtbybit.com/resources/nexuscore.80896/";

    /**
     * Runs when the plugin turns on. Loads config, registers commands/listeners,
     * and starts the update checker.
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        AbilityManager.init(this);
        Msg.printBanner(this, "BOOTED ✔️");
        UpdateChecker checker = new UpdateChecker(this, SPIGOT_ID, BBB_URL);
        updates = UpdateBootstrap.enable(this, checker);
        getServer().getPluginManager().registerEvents(new AdminJoinListener(this, SPIGOT_ID, BBB_URL), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(), this);
        CommandRegistrar.register(this);
        Systems.loadEnabledFeatures();
    }

    /** Runs when the plugin turns off. Stops banners and update tasks. */
    @Override
    public void onDisable() {
        Msg.printBanner(this, "SHUTDOWN ❌");
        if (updates != null) updates.disable();
    }
}
