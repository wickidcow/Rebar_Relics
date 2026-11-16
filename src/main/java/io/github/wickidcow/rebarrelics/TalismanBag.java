package io.github.wickidcow.rebarrelics;

import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Right-click access key. Stored items are keyed by player UUID on the server,
 * never encoded into the bag stack, so copied bag items share one personal store.
 */
public final class TalismanBag extends RebarItem implements InteractRebarItemHandler {
    public TalismanBag(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    @MultiHandler(priorities = {EventPriority.NORMAL, EventPriority.MONITOR})
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.useItemInHand() == Event.Result.DENY) {
            return;
        }
        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        int tier = switch (getKey().getKey()) {
            case "talisman_bag_1" -> 1;
            case "talisman_bag_2" -> 2;
            case "talisman_bag_3" -> 3;
            default -> 0;
        };
        if (tier > 0) RebarRelics.getPlugin(RebarRelics.class).bags().open(event.getPlayer(), tier);
    }
}
