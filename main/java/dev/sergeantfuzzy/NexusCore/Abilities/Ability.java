package dev.sergeantfuzzy.NexusCore.Abilities;

import dev.sergeantfuzzy.NexusCore.Utilities.i18n;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public enum Ability {
    BERSERK_RUSH(
            "berserk",
            "Berserk Rush",
            Material.NETHERITE_AXE,
            45,
            List.of(
                    "§7Explode with fury to gain Strength II",
                    "§7and Speed II for a short burst."
            ),
            (plugin, player) -> {
                addEffect(player, PotionEffectType.STRENGTH, 10, 1);
                addEffect(player, PotionEffectType.SPEED, 10, 1);
                player.getWorld().spawnParticle(Particle.CRIT, player.getLocation(), 40, 0.4, 0.2, 0.4);
                playSound(player, Sound.ITEM_TRIDENT_RIPTIDE_3, 1.2f, 0.8f);
            }
    ),
    GUARDIAN_SHELL(
            "guardian_shell",
            "Guardian Shell",
            Material.PRISMARINE_CRYSTALS,
            60,
            List.of(
                    "§7Summon a shimmering barrier granting",
                    "§7absorption, resistance, and water breathing."
            ),
            (plugin, player) -> {
                addEffect(player, PotionEffectType.ABSORPTION, 15, 3);
                addEffect(player, PotionEffectType.RESISTANCE, 12, 1);
                addEffect(player, PotionEffectType.WATER_BREATHING, 30, 0);
                player.getWorld().spawnParticle(Particle.NAUTILUS, player.getLocation(), 40, 0.5, 0.5, 0.5);
                playSound(player, Sound.BLOCK_CONDUIT_AMBIENT, 1.1f, 1.0f);
            }
    ),
    BLINK_STEP(
            "blink",
            "Blink Step",
            Material.ENDER_PEARL,
            25,
            List.of(
                    "§7Teleport up to 12 blocks along your",
                    "§7line-of-sight without taking damage."
            ),
            (plugin, player) -> {
                Location destination = findBlinkLocation(player, 12);
                if (destination != null) {
                    player.teleport(destination);
                    player.getWorld().spawnParticle(Particle.PORTAL, destination, 60, 0.6, 0.6, 0.6);
                    playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                } else {
                    i18n.sendMM(player, "<red>No safe location found for Blink Step.</red>");
                }
            }
    ),
    REGROWTH(
            "regrowth",
            "Regrowth",
            Material.GOLDEN_CARROT,
            35,
            List.of(
                    "§7Rapidly heal and cleanse yourself while",
                    "§7restoring hunger and saturation."
            ),
            (plugin, player) -> {
                player.setHealth(Math.min(player.getHealth() + 12.0, player.getMaxHealth()));
                player.setFoodLevel(20);
                player.setSaturation(20f);
                clearNegativeEffects(player);
                addEffect(player, PotionEffectType.REGENERATION, 6, 1);
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 20, 0.3, 0.3, 0.3);
                playSound(player, Sound.BLOCK_BREWING_STAND_BREW, 1.3f, 1.0f);
            }
    ),
    SHOCKWAVE(
            "shockwave",
            "Shockwave",
            Material.IRON_SWORD,
            40,
            List.of(
                    "§7Emit a concussive blast that damages",
                    "§7and hurls nearby mobs outward."
            ),
            (plugin, player) -> {
                List<LivingEntity> targets = nearbyLiving(player, 6);
                for (LivingEntity entity : targets) {
                    Vector push = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.2);
                    push.setY(0.5);
                    entity.setVelocity(push);
                    entity.damage(4.0, player);
                }
                player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 20, 0.3, 0.2, 0.3);
                playSound(player, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
            }
    ),
    INFERNO_BURST(
            "inferno",
            "Inferno Burst",
            Material.BLAZE_POWDER,
            50,
            List.of(
                    "§7Ignite hostile mobs around you while",
                    "§7gaining temporary fire immunity."
            ),
            (plugin, player) -> {
                for (LivingEntity entity : nearbyLiving(player, 5)) {
                    if (entity instanceof Monster) {
                        entity.setFireTicks(100);
                        entity.damage(3.0, player);
                    }
                }
                addEffect(player, PotionEffectType.FIRE_RESISTANCE, 12, 0);
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 100, 0.7, 0.4, 0.7);
                playSound(player, Sound.ITEM_FIRECHARGE_USE, 1.2f, 1.0f);
            }
    ),
    FROST_NOVA(
            "frost_nova",
            "Frost Nova",
            Material.BLUE_ICE,
            45,
            List.of(
                    "§7Freeze mobs in place with Slowness IV",
                    "§7while revealing them with Glowing."
            ),
            (plugin, player) -> {
                for (LivingEntity entity : nearbyLiving(player, 5)) {
                    if (entity.equals(player)) continue;
                    addEffect(entity, PotionEffectType.SLOWNESS, 6, 3);
                    addEffect(entity, PotionEffectType.GLOWING, 6, 0);
                }
                player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation(), 80, 0.6, 0.4, 0.6);
                playSound(player, Sound.BLOCK_GLASS_BREAK, 1.2f, 0.8f);
            }
    ),
    FEATHER_FALL(
            "feather_fall",
            "Feather Fall",
            Material.FEATHER,
            30,
            List.of(
                    "§7Gain Slow Falling and Jump Boost,",
                    "§7negating incoming fall damage."
            ),
            (plugin, player) -> {
                player.setFallDistance(0f);
                addEffect(player, PotionEffectType.SLOW_FALLING, 15, 0);
                addEffect(player, PotionEffectType.JUMP_BOOST, 15, 1);
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 30, 0.4, 0.2, 0.4);
                playSound(player, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.4f);
            }
    ),
    ENERGIZE(
            "energize",
            "Energize",
            Material.HONEY_BOTTLE,
            30,
            List.of(
                    "§7Refill hunger, saturation, and purge",
                    "§7lingering fatigue-based debuffs."
            ),
            (plugin, player) -> {
                player.setFoodLevel(20);
                player.setSaturation(20f);
                player.setExhaustion(0f);
                clearNegativeEffects(player);
                addEffect(player, PotionEffectType.DOLPHINS_GRACE, 5, 0);
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 40, 0.6, 0.5, 0.6);
                playSound(player, Sound.ITEM_HONEY_BOTTLE_DRINK, 1.0f, 1.0f);
            }
    ),
    LIFELINE(
            "lifeline",
            "Lifeline",
            Material.TOTEM_OF_UNDYING,
            75,
            List.of(
                    "§7Trigger an emergency burst of healing,",
                    "§7absorption, and regeneration."
            ),
            (plugin, player) -> {
                addEffect(player, PotionEffectType.REGENERATION, 8, 2);
                addEffect(player, PotionEffectType.ABSORPTION, 20, 3);
                addEffect(player, PotionEffectType.RESISTANCE, 6, 1);
                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 80, 0.7, 0.9, 0.7);
                playSound(player, Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
            }
    ),
    STONE_SKIN(
            "stone_skin",
            "Stone Skin",
            Material.SHIELD,
            50,
            List.of(
                    "§7Temporarily harden your body, gaining",
                    "§7Resistance IV but moving slower."
            ),
            (plugin, player) -> {
                addEffect(player, PotionEffectType.RESISTANCE, 8, 3);
                addEffect(player, PotionEffectType.SLOWNESS, 6, 1);
                player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation(), 40, 0.4, 0.4, 0.4, Material.STONE.createBlockData());
                playSound(player, Sound.BLOCK_ANVIL_LAND, 0.7f, 0.7f);
            }
    ),
    WIND_STEP(
            "wind_step",
            "Wind Step",
            Material.RABBIT_FOOT,
            25,
            List.of(
                    "§7Dash forward with a gust that grants",
                    "§7Speed III and Jump Boost II."
            ),
            (plugin, player) -> {
                Vector boost = player.getLocation().getDirection().normalize().multiply(0.8);
                boost.setY(0.4);
                player.setVelocity(player.getVelocity().add(boost));
                addEffect(player, PotionEffectType.SPEED, 8, 2);
                addEffect(player, PotionEffectType.JUMP_BOOST, 8, 2);
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 25, 0.4, 0.15, 0.4);
                playSound(player, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.7f, 1.4f);
            }
    ),
    THUNDERCLAP(
            "thunderclap",
            "Thunderclap",
            Material.TRIDENT,
            55,
            List.of(
                    "§7Call a lightning strike at your target,",
                    "§7damaging and stunning foes."
            ),
            (plugin, player) -> {
                Location target = targetLocation(player, 20);
                if (target == null) target = player.getLocation();
                player.getWorld().strikeLightningEffect(target);
                for (LivingEntity entity : entitiesNear(target, 3)) {
                    if (entity.equals(player)) continue;
                    entity.damage(6.0, player);
                    addEffect(entity, PotionEffectType.SLOWNESS, 4, 1);
                }
                playSound(player, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.2f, 1.0f);
            }
    ),
    VERDANT_BLESSING(
            "verdant",
            "Verdant Blessing",
            Material.BONE_MEAL,
            40,
            List.of(
                    "§7Coax nearby crops to grow instantly",
                    "§7while feeding yourself."
            ),
            (plugin, player) -> {
                World world = player.getWorld();
                Location base = player.getLocation();
                int grown = 0;
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        Block cropBlock = world.getBlockAt(base.getBlockX() + dx, base.getBlockY(), base.getBlockZ() + dz);
                        if (cropBlock.getBlockData() instanceof Ageable ageable) {
                            if (ageable.getAge() < ageable.getMaximumAge()) {
                                ageable.setAge(Math.min(ageable.getAge() + 2, ageable.getMaximumAge()));
                                cropBlock.setBlockData(ageable);
                                grown++;
                            }
                        }
                    }
                }
                if (grown > 0) {
                    world.spawnParticle(Particle.HAPPY_VILLAGER, base, 50, 1.5, 0.5, 1.5);
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + 4));
                    playSound(player, Sound.ITEM_BONE_MEAL_USE, 1.0f, 1.2f);
                } else {
                    i18n.sendMM(player, "<yellow>No crops nearby responded to Verdant Blessing.</yellow>");
                }
            }
    ),
    NIGHT_STALKER(
            "night_stalker",
            "Night Stalker",
            Material.PHANTOM_MEMBRANE,
            35,
            List.of(
                    "§7Slip into the shadows with Invisibility,",
                    "§7Night Vision, and Speed."
            ),
            (plugin, player) -> {
                addEffect(player, PotionEffectType.INVISIBILITY, 12, 0);
                addEffect(player, PotionEffectType.NIGHT_VISION, 30, 0);
                addEffect(player, PotionEffectType.SPEED, 12, 1);
                playSound(player, Sound.ENTITY_PHANTOM_AMBIENT, 0.7f, 0.8f);
            }
    ),
    ARCTIC_AURA(
            "arctic_aura",
            "Arctic Aura",
            Material.SNOWBALL,
            55,
            List.of(
                    "§7Freeze nearby water into frosted ice and",
                    "§7slow enemies caught in the chill."
            ),
            (plugin, player) -> {
                Location base = player.getLocation();
                List<BlockState> changed = new ArrayList<>();
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        Block block = base.clone().add(dx, -1, dz).getBlock();
                        if (block.getType() == Material.WATER) {
                            changed.add(block.getState());
                            block.setType(Material.FROSTED_ICE, false);
                        }
                    }
                }
                if (!changed.isEmpty()) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        for (BlockState state : changed) {
                            state.update(true, false);
                        }
                    }, 20L * 6);
                }
                for (LivingEntity entity : nearbyLiving(player, 5)) {
                    if (entity.equals(player)) continue;
                    addEffect(entity, PotionEffectType.SLOWNESS, 6, 2);
                }
                player.getWorld().spawnParticle(Particle.SNOWFLAKE, base, 70, 0.6, 0.4, 0.6);
                playSound(player, Sound.BLOCK_SNOW_PLACE, 1.0f, 1.1f);
            }
    ),
    FLAME_DASH(
            "flame_dash",
            "Flame Dash",
            Material.BLAZE_ROD,
            30,
            List.of(
                    "§7Propel yourself forward in a blaze,",
                    "§7igniting enemies while fireproof."
            ),
            (plugin, player) -> {
                Vector dash = player.getLocation().getDirection().normalize().multiply(1.2);
                dash.setY(0.2);
                player.setVelocity(player.getVelocity().add(dash));
                addEffect(player, PotionEffectType.FIRE_RESISTANCE, 8, 0);
                for (LivingEntity entity : nearbyLiving(player, 4)) {
                    if (entity.equals(player)) continue;
                    entity.setFireTicks(60);
                }
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 80, 0.4, 0.2, 0.4);
                playSound(player, Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.6f);
            }
    ),
    ARCANE_PULSE(
            "arcane_pulse",
            "Arcane Pulse",
            Material.AMETHYST_SHARD,
            50,
            List.of(
                    "§7Release a burst of arcane power that",
                    "§7damages and reveals enemies."
            ),
            (plugin, player) -> {
                player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 100, 0.8, 0.6, 0.8);
                playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.6f);
                for (LivingEntity entity : nearbyLiving(player, 6)) {
                    if (entity.equals(player)) continue;
                    entity.damage(5.0, player);
                    addEffect(entity, PotionEffectType.GLOWING, 6, 0);
                }
                player.giveExp(5);
            }
    ),
    MINERS_FOCUS(
            "miners_focus",
            "Miner's Focus",
            Material.DIAMOND_PICKAXE,
            45,
            List.of(
                    "§7Gain Haste II and Luck, perfect for",
                    "§7resource runs and excavation."
            ),
            (plugin, player) -> {
                addEffect(player, PotionEffectType.HASTE, 15, 1);
                addEffect(player, PotionEffectType.LUCK, 15, 0);
                playSound(player, Sound.BLOCK_DEEPSLATE_BREAK, 0.9f, 0.8f);
            }
    ),
    GRAVITY_WELL(
            "gravity_well",
            "Gravity Well",
            Material.NETHERITE_INGOT,
            60,
            List.of(
                    "§7Pull enemies toward you, suspending",
                    "§7them briefly for follow-up strikes."
            ),
            (plugin, player) -> {
                Location center = player.getLocation();
                for (LivingEntity entity : nearbyLiving(player, 7)) {
                    if (entity.equals(player)) continue;
                    Vector pull = center.toVector().subtract(entity.getLocation().toVector()).normalize().multiply(0.8);
                    pull.setY(0.4);
                    entity.setVelocity(pull);
                    addEffect(entity, PotionEffectType.LEVITATION, 2, 0);
                }
                player.getWorld().spawnParticle(Particle.PORTAL, center, 80, 0.8, 0.8, 0.8);
                playSound(player, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
            }
    ),
    RADIANT_HEAL(
            "radiant_heal",
            "Radiant Heal",
            Material.GOLDEN_APPLE,
            50,
            List.of(
                    "§7Release a healing pulse that restores",
                    "§7nearby allies and yourself."
            ),
            (plugin, player) -> {
                for (Entity entity : player.getNearbyEntities(6, 3, 6)) {
                    if (entity instanceof Player ally) {
                        ally.setHealth(Math.min(ally.getHealth() + 6.0, ally.getMaxHealth()));
                        addEffect(ally, PotionEffectType.REGENERATION, 6, 0);
                    }
                }
                player.setHealth(Math.min(player.getHealth() + 6.0, player.getMaxHealth()));
                player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 60, 0.7, 0.5, 0.7);
                playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
            }
    ),
    RIFT_RECALL(
            "rift_recall",
            "Rift Recall",
            Material.COMPASS,
            70,
            List.of(
                    "§7Teleport to your bed spawn (or world",
                    "§7spawn) while gaining brief resistance."
            ),
            (plugin, player) -> {
                Location bed = player.getBedSpawnLocation();
                if (bed == null) bed = player.getWorld().getSpawnLocation();
                player.teleport(bed);
                addEffect(player, PotionEffectType.RESISTANCE, 6, 1);
                player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, bed, 50, 0.5, 0.5, 0.5);
                playSound(player, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f);
            }
    ),
    TIDE_CALLER(
            "tide_caller",
            "Tide Caller",
            Material.HEART_OF_THE_SEA,
            55,
            List.of(
                    "§7Command the tides with Conduit Power,",
                    "§7Dolphin's Grace, and Water Breathing."
            ),
            (plugin, player) -> {
                addEffect(player, PotionEffectType.CONDUIT_POWER, 20, 0);
                addEffect(player, PotionEffectType.DOLPHINS_GRACE, 15, 0);
                addEffect(player, PotionEffectType.WATER_BREATHING, 30, 0);
                player.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, player.getLocation(), 80, 0.5, 1.0, 0.5);
                playSound(player, Sound.ENTITY_DOLPHIN_PLAY, 1.0f, 1.2f);
            }
    ),
    BEASTMASTER(
            "beastmaster",
            "Beastmaster",
            Material.BONE,
            60,
            List.of(
                    "§7Summon loyal wolves to guard you for",
                    "§7several minutes."
            ),
            (plugin, player) -> {
                for (int i = 0; i < 2; i++) {
                    Wolf wolf = (Wolf) player.getWorld().spawnEntity(player.getLocation(), EntityType.WOLF);
                    wolf.setOwner(player);
                    wolf.setAdult();
                    wolf.setCustomName("§e" + player.getName() + "§7’s Guardian");
                    wolf.setAngry(false);
                }
                playSound(player, Sound.ENTITY_WOLF_AMBIENT, 1.0f, 1.0f);
            }
    ),
    EARTHSHATTER(
            "earthshatter",
            "Earthshatter",
            Material.BASALT,
            55,
            List.of(
                    "§7Smash the ground, launching nearby mobs",
                    "§7and dealing heavy damage."
            ),
            (plugin, player) -> {
                Location center = player.getLocation();
                player.getWorld().spawnParticle(Particle.BLOCK, center, 80, 0.6, 0.2, 0.6, Material.DEEPSLATE.createBlockData());
                playSound(player, Sound.BLOCK_BASALT_BREAK, 1.0f, 0.7f);
                for (LivingEntity entity : nearbyLiving(player, 4)) {
                    if (entity.equals(player)) continue;
                    entity.damage(7.0, player);
                    Vector vec = entity.getLocation().toVector().subtract(center.toVector()).normalize().multiply(0.4);
                    vec.setY(0.9);
                    entity.setVelocity(vec);
                }
            }
    ),
    SKYBOUND_LEAP(
            "skybound_leap",
            "Skybound Leap",
            Material.ELYTRA,
            35,
            List.of(
                    "§7Launch skyward with controlled glide via",
                    "§7Slow Falling and brief Resistance."
            ),
            (plugin, player) -> {
                Vector lift = player.getLocation().getDirection().normalize().multiply(0.4);
                lift.setY(lift.getY() + 1.1);
                player.setVelocity(lift);
                addEffect(player, PotionEffectType.SLOW_FALLING, 12, 0);
                addEffect(player, PotionEffectType.RESISTANCE, 6, 0);
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 50, 0.5, 0.4, 0.5);
                playSound(player, Sound.ITEM_ELYTRA_FLYING, 0.8f, 1.3f);
            }
    ),
    SOLAR_FLARE(
            "solar_flare",
            "Solar Flare",
            Material.SUNFLOWER,
            45,
            List.of(
                    "§7Channel sunlight to burn undead mobs and",
                    "§7grant yourself Absorption."
            ),
            (plugin, player) -> {
                for (LivingEntity entity : nearbyLiving(player, 6)) {
                    if (entity.equals(player)) continue;
                    if (isUndead(entity.getType())) {
                        entity.setFireTicks(80);
                        entity.damage(6.0, player);
                    } else if (entity instanceof Monster) {
                        entity.damage(3.0, player);
                    }
                }
                addEffect(player, PotionEffectType.ABSORPTION, 12, 2);
                player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 70, 0.5, 0.5, 0.5);
                playSound(player, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.3f);
            }
    );

    private final String id;
    private final String displayName;
    private final Material icon;
    private final int defaultCooldownSeconds;
    private final List<String> description;
    private final AbilityAction action;

    Ability(String id,
            String displayName,
            Material icon,
            int defaultCooldownSeconds,
            List<String> description,
            AbilityAction action) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.defaultCooldownSeconds = defaultCooldownSeconds;
        this.description = List.copyOf(description);
        this.action = action;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public Material icon() {
        return icon;
    }

    public int defaultCooldownSeconds() {
        return defaultCooldownSeconds;
    }

    public List<String> description() {
        return description;
    }

    public String permissionNode() {
        return "nexuscore.abilities.use." + id;
    }

    public void execute(JavaPlugin plugin, Player player) {
        action.execute(plugin, player);
    }

    public static Ability fromInput(String input) {
        if (input == null || input.isEmpty()) return null;
        String normalized = input.toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "");
        for (Ability ability : values()) {
            if (ability.id.equalsIgnoreCase(normalized)) return ability;
            String displayNormalized = ability.displayName.toLowerCase(Locale.ROOT).replace(" ", "");
            if (displayNormalized.equals(normalized)) return ability;
            if (ability.name().toLowerCase(Locale.ROOT).equals(normalized)) return ability;
        }
        return null;
    }

    private static List<LivingEntity> nearbyLiving(Player player, double radius) {
        List<LivingEntity> list = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living) {
                list.add(living);
            }
        }
        return list;
    }

    private static List<LivingEntity> entitiesNear(Location location, double radius) {
        List<LivingEntity> list = new ArrayList<>();
        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (entity instanceof LivingEntity living) {
                list.add(living);
            }
        }
        return list;
    }

    private static void addEffect(LivingEntity entity, PotionEffectType type, int seconds, int amplifier) {
        if (type == null) return;
        entity.addPotionEffect(new PotionEffect(type, seconds * 20, amplifier, true, true, true));
    }

    private static void clearNegativeEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (NEGATIVE_EFFECTS.contains(effect.getType())) {
                player.removePotionEffect(effect.getType());
            }
        }
    }

    private static Location findBlinkLocation(Player player, int maxDistance) {
        Location target = targetLocation(player, maxDistance);
        if (target == null) return null;
        Location safe = target.clone();
        safe.setYaw(player.getLocation().getYaw());
        safe.setPitch(player.getLocation().getPitch());
        safe.add(0.5, 0, 0.5);
        return safe;
    }

    private static Location targetLocation(Player player, int maxDistance) {
        Block block = player.getTargetBlockExact(maxDistance);
        if (block != null) {
            Location loc = block.getLocation().clone();
            if (!block.getType().isAir()) loc.add(0, 1, 0);
            return loc;
        }
        return player.getLocation().add(player.getLocation().getDirection().normalize().multiply(maxDistance / 2.0));
    }

    private static void playSound(Player player, Sound sound, float volume, float pitch) {
        player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
    }

    private static boolean isUndead(EntityType type) {
        return UNDEAD_TYPES.contains(type);
    }

    private static final Set<PotionEffectType> NEGATIVE_EFFECTS =
            Set.of(
                    PotionEffectType.BLINDNESS,
                    PotionEffectType.NAUSEA,
                    PotionEffectType.DARKNESS,
                    PotionEffectType.INSTANT_DAMAGE,
                    PotionEffectType.HUNGER,
                    PotionEffectType.LEVITATION,
                    PotionEffectType.POISON,
                    PotionEffectType.SLOWNESS,
                    PotionEffectType.MINING_FATIGUE,
                    PotionEffectType.UNLUCK,
                    PotionEffectType.WEAKNESS,
                    PotionEffectType.WITHER
            );

    private static final Set<EntityType> UNDEAD_TYPES = EnumSet.of(
            EntityType.ZOMBIE,
            EntityType.DROWNED,
            EntityType.HUSK,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.SKELETON,
            EntityType.STRAY,
            EntityType.WITHER_SKELETON,
            EntityType.PHANTOM,
            EntityType.ZOGLIN
    );
}
