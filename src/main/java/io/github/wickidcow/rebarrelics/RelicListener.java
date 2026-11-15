package io.github.wickidcow.rebarrelics;

import io.github.wickidcow.rebarrelics.RelicItems.Type;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/** Per-hit effects use only the highest inventory tier for each family. */
public final class RelicListener implements Listener {
    private final RebarRelics plugin;

    public RelicListener(RebarRelics plugin) {
        this.plugin = plugin;
    }

    private int tier(Player player, Type type, String configKey) {
        if (!plugin.getConfig().getBoolean("talismans." + configKey + ".enabled", true)) return 0;
        return RelicItems.bestTier(player, type,
                plugin.getConfig().getBoolean("include-offhand", true));
    }

    private double multiplier(String key, int level) {
        if (level == 0) return 1.0;
        double[] defaults = key.equals("archery")
                ? new double[] {1.0, 1.10, 1.20, 1.40}
                : new double[] {1.0, 1.05, 1.10, 1.20};
        double value = plugin.getConfig().getDouble("talismans." + key + ".level-" + level,
                level == 1
                        ? plugin.getConfig().getDouble("talismans." + key + ".multiplier", defaults[level])
                        : defaults[level]);
        return Double.isFinite(value) ? Math.clamp(value, 1.0, 5.0) : 1.0;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void damage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            int level = tier(player, Type.STRENGTH, "strength");
            if (level > 0) event.setDamage(event.getDamage() * multiplier("strength", level));
            return;
        }

        if (event.getDamager() instanceof Arrow arrow
                && arrow.getShooter() instanceof Player player) {
            double factor = multiplier("archery", tier(player, Type.ARCHERY, "archery"))
                    * multiplier("strength", tier(player, Type.STRENGTH, "strength"));
            if (factor != 1.0) event.setDamage(event.getDamage() * factor);
        } else if (event.getDamager() instanceof Trident trident
                && trident.getShooter() instanceof Player player) {
            int level = tier(player, Type.STRENGTH, "strength");
            if (level > 0) event.setDamage(event.getDamage() * multiplier("strength", level));
        }
    }
}
