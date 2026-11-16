package io.github.wickidcow.rebarrelics;

import io.github.wickidcow.rebarrelics.RelicItems.Type;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Original mechanics inspired by the reference Eco talismans.
 * Vanilla bosses are explicitly configured; custom MythicMobs are not assumed to
 * be bosses without a reliable API integration.
 */
public final class AdvancedRelicListener implements Listener {
    private final RebarRelics plugin;
    private final Set<EntityType> bossTypes = EnumSet.noneOf(EntityType.class);

    public AdvancedRelicListener(RebarRelics plugin) {
        this.plugin = plugin;
        for (String name : plugin.getConfig().getStringList("boss-types")) {
            try {
                bossTypes.add(EntityType.valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Unknown boss entity type in config.yml: " + name);
            }
        }
    }

    private int tier(Player player, Type type, String key) {
        if (!plugin.getConfig().getBoolean("talismans." + key + ".enabled", true)) return 0;
        return RelicItems.bestTier(player, type,
                plugin.getConfig().getBoolean("include-offhand", true));
    }

    private double multiplier(String key, int tier) {
        if (tier == 0) return 1.0;
        double[] defaults = switch (key) {
            case "boss" -> new double[] {1, 1.05, 1.10, 1.15};
            case "poseidon" -> new double[] {1, 1.05, 1.10, 1.20};
            case "resistance" -> new double[] {1, 0.95, 0.90, 0.80};
            default -> throw new IllegalArgumentException("Unknown relic type " + key);
        };
        double value = plugin.getConfig().getDouble("talismans." + key + ".level-" + tier, defaults[tier]);
        return Double.isFinite(value) ? Math.clamp(value, 0.1, 2.0) : defaults[tier];
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim) {
            int resist = tier(victim, Type.RESISTANCE, "resistance");
            if (resist > 0) {
                event.setDamage(event.getDamage() * multiplier("resistance", resist));
            }
        }

        Player attacker = null;
        if (event.getDamager() instanceof Player player) {
            attacker = player;
        } else if (event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player player) {
            attacker = player;
        }
        if (attacker == null) return;

        double modifier = 1.0;
        if (bossTypes.contains(event.getEntity().getType())) {
            modifier *= multiplier("boss", tier(attacker, Type.BOSS, "boss"));
        }
        if (event.getDamager() instanceof Trident
                || (event.getDamager() instanceof Player
                        && attacker.getInventory().getItemInMainHand().getType() == Material.TRIDENT)) {
            modifier *= multiplier("poseidon", tier(attacker, Type.POSEIDON, "poseidon"));
        }
        if (modifier != 1.0) event.setDamage(event.getDamage() * modifier);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onImmunity(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Type type;
        switch (event.getCause()) {
            case POISON -> type = Type.IMMUNITY;
            case WITHER -> type = Type.NECROSIS;
            default -> { return; }
        }
        String key = type == Type.IMMUNITY ? "immunity" : "necrosis";
        if (tier(player, type, key) > 0) event.setCancelled(true);
    }
}
