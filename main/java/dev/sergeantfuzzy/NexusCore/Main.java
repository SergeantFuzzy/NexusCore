package dev.sergeantfuzzy.NexusCore;

import dev.sergeantfuzzy.NexusCore.Commands.CommandRegistrar;
import dev.sergeantfuzzy.NexusCore.Listeners.AdminJoinListener;
import dev.sergeantfuzzy.NexusCore.Listeners.LeaveListener;
import dev.sergeantfuzzy.NexusCore.Utilities.UpdateBootstrap;
import dev.sergeantfuzzy.NexusCore.Utilities.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;
import dev.sergeantfuzzy.NexusCore.Utilities.Msg;

public final class Main extends JavaPlugin {
    private UpdateBootstrap updates;
    public UpdateBootstrap getUpdates() { return this.updates; }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Msg.printBanner(this, "BOOTED ✔️");
        updates = UpdateBootstrap.enable(this, new UpdateChecker(this, /*spigotId*/ 129311, /*bbbUrl*/ null));
        getServer().getPluginManager().registerEvents(new AdminJoinListener(this, 129311, "https://builtbybit.com/resources/your-resource-slug-or-id/"), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(), this);
        CommandRegistrar.register(this);
    }
    @Override
    public void onDisable() {
        Msg.printBanner(this, "SHUTDOWN ❌");
        if (updates != null) updates.disable();
    }
}