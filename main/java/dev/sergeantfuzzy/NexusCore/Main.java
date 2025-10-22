package dev.sergeantfuzzy.NexusCore;

import dev.sergeantfuzzy.NexusCore.Commands.CommandRegistrar;
import dev.sergeantfuzzy.NexusCore.Listeners.AdminJoinListener;
import dev.sergeantfuzzy.NexusCore.Listeners.LeaveListener;
import dev.sergeantfuzzy.NexusCore.System.Systems;
import dev.sergeantfuzzy.NexusCore.Utilities.UpdateBootstrap;
import dev.sergeantfuzzy.NexusCore.Utilities.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;
import dev.sergeantfuzzy.NexusCore.Utilities.Msg;

public final class Main extends JavaPlugin {
    private UpdateBootstrap updates;
    public UpdateBootstrap getUpdates() { return this.updates; }
    private static final int SPIGOT_ID = 129669;
    private static final String BBB_URL   = "https://builtbybit.com/resources/nexuscore.80896/";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Msg.printBanner(this, "BOOTED ✔️");
        UpdateChecker checker = new UpdateChecker(this, SPIGOT_ID, BBB_URL);
        updates = UpdateBootstrap.enable(this, checker);
        getServer().getPluginManager().registerEvents(new AdminJoinListener(this, SPIGOT_ID, BBB_URL), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(), this);
        CommandRegistrar.register(this);
        Systems.loadEnabledFeatures();
    }
    @Override
    public void onDisable() {
        Msg.printBanner(this, "SHUTDOWN ❌");
        if (updates != null) updates.disable();
    }
}