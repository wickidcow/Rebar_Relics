package io.github.wickidcow.rebarrelics;

import io.github.wickidcow.rebarrelics.RelicItems.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/** A fresh implementation of six classic-style talisman effects. */
public final class ClassicTalismansListener implements Listener {
    private static final int BUFF_TICKS = 3 * 60 * 20;
    private final RebarRelics plugin;

    public ClassicTalismansListener(RebarRelics plugin) {
        this.plugin = plugin;
    }

    private static String configName(Type type) {
        return switch (type) {
            case ANGEL -> "angel";
            case TRAVELLER -> "traveller";
            case FIREFIGHTER -> "firefighter";
            case WATER_BREATHER -> "water-breather";
            case LAVA_WALKER -> "lava-walker";
            case ANVIL -> "anvil";
            default -> throw new IllegalArgumentException("Not a classic talisman: " + type);
        };
    }

    private boolean enabled(Type type) {
        return plugin.getConfig().getBoolean("talismans." + configName(type) + ".enabled", true);
    }

    private boolean includeOffhand() {
        return plugin.getConfig().getBoolean("include-offhand", true);
    }

    private boolean has(Player player, Type type) {
        return enabled(type) && RelicItems.has(player, type, includeOffhand());
    }

    private boolean consume(Player player, Type type) {
        return enabled(type) && RelicItems.consume(player, type, includeOffhand());
    }

    private boolean roll(Type type) {
        double chance = plugin.getConfig().getDouble("talismans."
                + configName(type) + ".chance", 1.0);
        return ChanceRoll.success(chance, ThreadLocalRandom.current().nextDouble());
    }

    private static void grant(Player player, PotionEffectType effect) {
        player.addPotionEffect(new PotionEffect(effect, BUFF_TICKS, 0, false, true, true));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        switch (event.getCause()) {
            case FALL -> {
                if (has(player, Type.ANGEL) && roll(Type.ANGEL)) event.setCancelled(true);
            }
            case DROWNING -> {
                if (!player.hasPotionEffect(PotionEffectType.WATER_BREATHING)
                        && consume(player, Type.WATER_BREATHER)) {
                    event.setCancelled(true);
                    grant(player, PotionEffectType.WATER_BREATHING);
                    player.setRemainingAir(player.getMaximumAir());
                }
            }
            case FIRE, FIRE_TICK -> {
                if (!player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)
                        && consume(player, Type.FIREFIGHTER)) {
                    event.setCancelled(true);
                    grant(player, PotionEffectType.FIRE_RESISTANCE);
                }
            }
            case LAVA -> {
                if (!player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)
                        && consume(player, Type.LAVA_WALKER)) {
                    event.setCancelled(true);
                    grant(player, PotionEffectType.FIRE_RESISTANCE);
                }
            }
            default -> { }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSprint(PlayerToggleSprintEvent event) {
        if (!event.isSprinting()) return;
        Player player = event.getPlayer();
        if (!has(player, Type.TRAVELLER) || player.hasPotionEffect(PotionEffectType.SPEED)) return;
        if (roll(Type.TRAVELLER)) grant(player, PotionEffectType.SPEED);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (!has(event.getPlayer(), Type.ANVIL)) return;
        ItemStack stack = event.getItem();
        ItemMeta meta = stack.getItemMeta();
        if (!(meta instanceof Damageable damageable)) return;
        int maxDurability = stack.getType().getMaxDurability();
        if (maxDurability <= 0 || event.getDamage() <= 0) return;
        if (damageable.getDamage() + event.getDamage() < maxDurability) return;

        if (!consume(event.getPlayer(), Type.ANVIL)) return;
        damageable.setDamage(0);
        stack.setItemMeta(meta);
        event.setDamage(0);
    }
}
