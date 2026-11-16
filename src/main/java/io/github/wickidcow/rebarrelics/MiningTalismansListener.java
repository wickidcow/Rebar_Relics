package io.github.wickidcow.rebarrelics;

import io.github.wickidcow.rebarrelics.RelicItems.Type;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Slimefun-inspired ore perks, restricted to vanilla ores and vanilla drops.
 * The Miner increases amounts on existing event drops and NEVER spawns items
 * separately, which keeps later drop cancellation and protections effective.
 */
public final class MiningTalismansListener implements Listener {
    private final RebarRelics plugin;

    public MiningTalismansListener(RebarRelics plugin) {
        this.plugin = plugin;
    }

    private boolean has(Player player, Type type, String key) {
        return plugin.getConfig().getBoolean("talismans." + key + ".enabled", true)
                && RelicItems.has(player, type,
                    plugin.getConfig().getBoolean("include-offhand", true));
    }

    private boolean success(String key) {
        double chance = plugin.getConfig().getDouble("talismans." + key + ".chance", 0.0);
        return ChanceRoll.success(chance, ThreadLocalRandom.current().nextDouble());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onOreDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL
                || !OreRules.isOre(event.getBlockState().getType().name())
                || !has(player, Type.MINER, "miner")
                || player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)
                || !success("miner")) return;

        for (Item drop : event.getItems()) {
            ItemStack stack = drop.getItemStack();
            if (!OreRules.canSafelyDouble(stack.getType().name(),
                    stack.hasItemMeta(), stack.getAmount(), stack.getMaxStackSize())) continue;
            ItemStack doubled = stack.clone();
            doubled.setAmount(stack.getAmount() * 2);
            drop.setItemStack(doubled);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onOreBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL
                || !OreRules.isOre(event.getBlock().getType().name())
                || !has(player, Type.CAVEMAN, "caveman")
                || !success("caveman")) return;

        PotionEffect haste = player.getPotionEffect(PotionEffectType.HASTE);
        int ticks = 20 * 20;
        // No duplicate activation when a better/equal active effect is present.
        if (haste != null && haste.getAmplifier() >= 1
                && haste.getDuration() >= ticks) return;

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.HASTE, ticks, 1, false, true, true));
    }
}
