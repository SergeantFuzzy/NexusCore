package dev.sergeantfuzzy.NexusCore.Update;

import org.bukkit.command.CommandSender;

public interface UpdateCheckService {
    void checkNow(CommandSender notifyTo, boolean announceIfLatest);
}