package io.github.wickidcow.rebarrelics;

import io.github.wickidcow.rebarrelics.RelicItems.Type;
import org.bukkit.GameMode;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Classic-inspired Whirlwind talisman, using *safe deflection* rather than
 * spawning a second projectile. The hit is cancelled: no arrow is copied,
 * redirected to a different shooter or made collectible twice.
 * Tridents are excluded deliberately.
 */
public final class WhirlwindListener implements Listener {
    private final RebarRelics plugin;

    public WhirlwindListener(RebarRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR
                || !(event.getDamager() instanceof AbstractArrow arrow)) {
            return;
        }

        if (!plugin.getConfig().getBoolean("talismans.whirlwind.enabled", true)) return;
        int tier = RelicItems.bestTier(player, Type.WHIRLWIND,
                plugin.getConfig().getBoolean("include-offhand", true));
        if (tier == 0) return;

        double[] defaults = {0.0, 0.12, 0.22, 0.35};
        double chance = plugin.getConfig().getDouble(
                "talismans.whirlwind.level-" + tier, defaults[tier]);
        if (WhirlwindRules.shouldDeflect(true, arrow instanceof Trident,
                arrow.getShooter() == player, event.getFinalDamage(),
                chance, ThreadLocalRandom.current().nextDouble())) {
            // Cancel only damage. Never spawn, clone, redirect or modify item ownership.
            event.setCancelled(true);
        }
    }
}
