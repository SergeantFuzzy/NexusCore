package dev.sergeantfuzzy.NexusCore.Abilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AbilityManager {
    private static AbilityManager instance;
    private final JavaPlugin plugin;
    private final Map<UUID, EnumMap<Ability, Long>> usage = new ConcurrentHashMap<>();
    private final EnumMap<Ability, AbilitySettings> settings = new EnumMap<>(Ability.class);
    private boolean enabled = true;
    private long defaultCooldownSeconds = 45;

    private AbilityManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reloadSettings();
    }

    public static AbilityManager init(JavaPlugin plugin) {
        instance = new AbilityManager(plugin);
        return instance;
    }

    public static AbilityManager instance() {
        if (instance == null) throw new IllegalStateException("AbilityManager has not been initialized.");
        return instance;
    }

    public static boolean isReady() {
        return instance != null;
    }

    public void reloadSettings() {
        plugin.saveDefaultConfig();
        FileConfiguration cfg = plugin.getConfig();
        enabled = cfg.getBoolean("Abilities.Enabled", true);
        defaultCooldownSeconds = Math.max(1L, cfg.getLong("Abilities.DefaultCooldownSeconds", 45L));
        settings.clear();
        ConfigurationSection perAbility = cfg.getConfigurationSection("Abilities.PerAbility");
        for (Ability ability : Ability.values()) {
            ConfigurationSection section = perAbility == null ? null : perAbility.getConfigurationSection(ability.id());
            boolean abilityEnabled = section == null || section.getBoolean("Enabled", true);
            long cooldown = section == null
                    ? ability.defaultCooldownSeconds()
                    : section.getLong("CooldownSeconds", ability.defaultCooldownSeconds());
            if (cooldown <= 0) cooldown = ability.defaultCooldownSeconds();
            settings.put(ability, new AbilitySettings(abilityEnabled, cooldown));
        }
    }

    public AbilityResult activate(Player player, Ability ability) {
        if (!enabled) return AbilityResult.disabledGlobal();
        AbilitySettings abilitySettings = settings.getOrDefault(
                ability,
                new AbilitySettings(true, Math.max(defaultCooldownSeconds, ability.defaultCooldownSeconds()))
        );
        if (!abilitySettings.enabled()) return AbilityResult.disabledAbility();
        long remaining = remainingCooldownMillis(player.getUniqueId(), ability, abilitySettings.cooldownSeconds());
        if (remaining > 0) {
            return AbilityResult.onCooldown((remaining + 999) / 1000);
        }
        ability.execute(plugin, player);
        markUsed(player.getUniqueId(), ability);
        return AbilityResult.successResult();
    }

    public long remainingCooldownSeconds(Player player, Ability ability) {
        AbilitySettings abilitySettings = settings.getOrDefault(
                ability,
                new AbilitySettings(true, Math.max(defaultCooldownSeconds, ability.defaultCooldownSeconds()))
        );
        long ms = remainingCooldownMillis(player.getUniqueId(), ability, abilitySettings.cooldownSeconds());
        return ms <= 0 ? 0 : (ms + 999) / 1000;
    }

    public boolean abilitiesEnabled() {
        return enabled;
    }

    public boolean isAbilityEnabled(Ability ability) {
        AbilitySettings abilitySettings = settings.get(ability);
        return abilitySettings == null || abilitySettings.enabled();
    }

    public long configuredCooldown(Ability ability) {
        return settings.getOrDefault(
                ability,
                new AbilitySettings(true, Math.max(defaultCooldownSeconds, ability.defaultCooldownSeconds()))
        ).cooldownSeconds();
    }

    private void markUsed(UUID uuid, Ability ability) {
        usage.computeIfAbsent(uuid, __ -> new EnumMap<>(Ability.class))
                .put(ability, System.currentTimeMillis());
    }

    private long remainingCooldownMillis(UUID uuid, Ability ability, long cooldownSeconds) {
        EnumMap<Ability, Long> record = usage.get(uuid);
        if (record == null) return 0;
        Long lastUsed = record.get(ability);
        if (lastUsed == null) return 0;
        long readyAt = lastUsed + (cooldownSeconds * 1000L);
        long now = System.currentTimeMillis();
        return Math.max(0, readyAt - now);
    }

    private record AbilitySettings(boolean enabled, long cooldownSeconds) {}

    public record AbilityResult(Status status, long secondsRemaining) {
        public static AbilityResult disabledGlobal() { return new AbilityResult(Status.DISABLED_GLOBAL, -1); }
        public static AbilityResult disabledAbility() { return new AbilityResult(Status.DISABLED_ABILITY, -1); }
        public static AbilityResult onCooldown(long seconds) { return new AbilityResult(Status.ON_COOLDOWN, Math.max(1, seconds)); }
        public static AbilityResult successResult() { return new AbilityResult(Status.SUCCESS, 0); }
        public boolean isSuccess() { return status == Status.SUCCESS; }
    }

    public enum Status {
        SUCCESS,
        DISABLED_GLOBAL,
        DISABLED_ABILITY,
        ON_COOLDOWN
    }
}
