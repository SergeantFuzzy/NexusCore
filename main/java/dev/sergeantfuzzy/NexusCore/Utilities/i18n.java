package dev.sergeantfuzzy.NexusCore.Utilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

public final class i18n {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_AMP = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private i18n() {}
    public static Component mm(String miniMessage) {
        return MM.deserialize(miniMessage == null ? "" : miniMessage);
    }
    public static Component mm(String miniMessage, TagResolver... resolvers) {
        return MM.deserialize(miniMessage == null ? "" : miniMessage, resolvers);
    }
    public static Component legacy(String legacyText) {
        return LEGACY_AMP.deserialize(legacyText == null ? "" : legacyText);
    }
    public static Component auto(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        if (text.indexOf('<') >= 0 && text.indexOf('>') > text.indexOf('<')) {
            return mm(text);
        }
        return legacy(text);
    }
    public static void sendMM(CommandSender sender, String miniMessage) {
        sender.sendMessage(mm(miniMessage));
    }
    public static void sendMM(CommandSender sender, String miniMessage, TagResolver... resolvers) {
        sender.sendMessage(mm(miniMessage, resolvers));
    }
    public static void sendLegacy(CommandSender sender, String legacyColored) {
        sender.sendMessage(legacy(legacyColored));
    }
    public static void sendAuto(CommandSender sender, String text) {
        sender.sendMessage(auto(text));
    }
    public static void send(CommandSender sender, Component component) {
        sender.sendMessage(component == null ? Component.empty() : component);
    }
    public static void sendUsage(CommandSender sender, String usageLine) {
        sendMM(sender, "<red><bold>USAGE</bold></red> <gray>»</gray> " + (usageLine == null ? "" : usageLine));
    }
}