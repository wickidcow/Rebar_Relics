package io.github.wickidcow.rebarrelics;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Conservative, single-view GUI. Only plain left/right clicks move a complete
 * stack into/out of one empty slot. Shift-click, swap, drag, creative and nesting
 * paths are intentionally blocked. The personal storage is canonical on disk.
 *
 * Bags are access keys; no item contains embedded inventory bytes.
 * Contents never contribute to inventory-based talisman effects.
 */
public final class TalismanBagService implements Listener {
    private final RebarRelics plugin;
    private final BagStore store;
    private final Map<UUID, BagSession> active = new HashMap<>();

    public TalismanBagService(RebarRelics plugin) {
        this.plugin = plugin;
        this.store = new BagStore(plugin.getDataFolder().toPath());
    }

    private static final class BagHolder implements InventoryHolder {
        private final UUID owner;
        private Inventory inventory;

        BagHolder(UUID owner) { this.owner = owner; }

        @Override
        public Inventory getInventory() { return inventory; }
    }

    private static final class BagSession {
        private final UUID owner;
        private final int tier;
        private final BagHolder holder;
        private final Inventory inventory;
        private ItemStack[] items;

        BagSession(UUID owner, int tier, BagHolder holder, Inventory inventory, ItemStack[] items) {
            this.owner = owner;
            this.tier = tier;
            this.holder = holder;
            this.inventory = inventory;
            this.items = items;
        }
    }

    public void open(Player player, int tier) {
        if (!plugin.getConfig().getBoolean("bags.enabled", false)) {
            player.sendMessage("Talisman bags are disabled on this server.");
            return;
        }
        // Keep creative/spectator blocked regardless of config until these
        // distinct client inventory paths have dedicated interaction tests.
        if (player.getGameMode() != GameMode.SURVIVAL
                && player.getGameMode() != GameMode.ADVENTURE) {
            player.sendMessage("Talisman bag storage is available only in survival/adventure.");
            return;
        }
        if (RelicItems.bestTier(player, RelicItems.Type.BAG, true) < tier) {
            player.sendMessage("You must carry the bag to access its storage.");
            return;
        }

        // Opening a second bag does not create a second independent container.
        if (active.containsKey(player.getUniqueId())) player.closeInventory();

        ItemStack[] contents;
        try {
            contents = store.load(player.getUniqueId());
        } catch (IOException ex) {
            plugin.getLogger().severe("Unable to open talisman bag for " + player.getUniqueId()
                    + ": " + ex.getMessage());
            player.sendMessage("Talisman bag storage could not be read. Nothing was modified.");
            return;
        }

        BagHolder holder = new BagHolder(player.getUniqueId());
        Inventory ui = Bukkit.createInventory(holder, BagRules.BACKING_SLOTS,
                Component.text("Talisman Bag " + tier + " - " + BagRules.capacity(tier) + " slots"));
        holder.inventory = ui;
        for (int slot = 0; slot < BagRules.BACKING_SLOTS; slot++) {
            if (BagRules.accessible(tier, slot)) {
                if (contents[slot] != null) ui.setItem(slot, contents[slot].clone());
            } else {
                ui.setItem(slot, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            }
        }

        BagSession session = new BagSession(player.getUniqueId(), tier, holder, ui, contents);
        active.put(player.getUniqueId(), session);
        player.openInventory(ui);
    }

    private BagSession session(Inventory top) {
        if (!(top.getHolder() instanceof BagHolder holder)) return null;
        BagSession found = active.get(holder.owner);
        return found != null && found.holder == holder && found.inventory == top ? found : null;
    }

    private void closeNextTick(Player player) {
        // Bukkit forbids closing inventory directly inside InventoryClickEvent.
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) player.closeInventory();
        });
    }

    private static boolean matchesRenderedSlot(ItemStack rendered, ItemStack stored) {
        boolean renderedEmpty = rendered == null || rendered.isEmpty();
        boolean storedEmpty = stored == null || stored.isEmpty();
        return renderedEmpty == storedEmpty && (renderedEmpty
                || (rendered.getAmount() == stored.getAmount()
                && rendered.isSimilar(stored)));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void click(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof BagHolder)) return;
        // A stale/custom orphaned bag view is never allowed to fall back to
        // vanilla inventory behavior when our active session is absent.
        event.setCancelled(true);
        BagSession bag = session(top);
        if (bag == null) return;
        if (!(event.getWhoClicked() instanceof Player player)
                || !bag.owner.equals(player.getUniqueId())) return;
        if ((player.getGameMode() != GameMode.SURVIVAL
                && player.getGameMode() != GameMode.ADVENTURE)
                || !plugin.getConfig().getBoolean("bags.enabled", false)
                || RelicItems.bestTier(player, RelicItems.Type.BAG, true) < bag.tier) {
            closeNextTick(player);
            return;
        }

        boolean plain = event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT;
        if (!plain) return;

        if (event.getClickedInventory() != bag.inventory) {
            // Normal inventory selection is permitted, but no bag item itself may
            // be moved while the personal container is open.
            if (event.getClickedInventory() == player.getInventory()
                    && !RelicItems.isBag(event.getCurrentItem())
                    && !RelicItems.isBag(event.getCursor())) {
                event.setCancelled(false);
            }
            return;
        }

        int slot = event.getRawSlot();
        if (!BagRules.accessible(bag.tier, slot)) return;

        ItemStack cursor = event.getCursor();
        ItemStack existing = bag.items[slot];
        if (!matchesRenderedSlot(event.getCurrentItem(), existing)) {
            plugin.getLogger().severe("Talisman Bag UI desynchronized from the saved session for "
                    + bag.owner + " at slot " + slot + "; refusing transfer");
            closeNextTick(player);
            return;
        }
        boolean cursorEmpty = cursor == null || cursor.isEmpty();
        BagTransferRules.Action action = BagTransferRules.choose(
                bag.tier, slot, cursorEmpty, existing == null,
                cursorEmpty || RelicItems.isStorableTalisman(cursor),
                cursorEmpty ? 0 : cursor.getAmount(),
                cursorEmpty ? 0 : cursor.getMaxStackSize());
        if (action == BagTransferRules.Action.NONE) return;

        ItemStack[] pending = bag.items.clone();
        ItemStack cursorAfter;
        ItemStack topAfter;
        if (action == BagTransferRules.Action.WITHDRAW) {
            // Withdraw exactly one complete stack. Never merge automatically.
            topAfter = null;
            cursorAfter = existing.clone();
            pending[slot] = null;
        } else {
            // Deposit exactly one complete, whitelisted talisman stack.
            topAfter = cursor.clone();
            cursorAfter = null;
            pending[slot] = topAfter.clone();
        }

        // A failed disk write leaves the inventory and cursor untouched.
        // Files are replaced atomically; the last known state is never parsed
        // from incomplete YAML on restart.
        try {
            store.saveIfUnchanged(player.getUniqueId(), bag.items, pending);
            bag.items = pending;
            bag.inventory.setItem(slot, topAfter);
            player.setItemOnCursor(cursorAfter == null
                    ? new ItemStack(Material.AIR) : cursorAfter);
        } catch (IOException ex) {
            plugin.getLogger().severe("Bag storage write failed for " + bag.owner
                    + ": " + ex.getMessage());
            player.sendMessage("Talisman bag storage could not be saved. Transfer cancelled.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void drag(InventoryDragEvent event) {
        // Deny even for a stale bag view whose active session was retired.
        if (event.getView().getTopInventory().getHolder() instanceof BagHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void close(InventoryCloseEvent event) {
        BagSession bag = session(event.getView().getTopInventory());
        if (bag != null && bag.owner.equals(event.getPlayer().getUniqueId())) {
            active.remove(bag.owner);
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        active.remove(event.getPlayer().getUniqueId());
    }

    public void closeAll() {
        for (UUID id : java.util.List.copyOf(active.keySet())) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) player.closeInventory();
        }
        active.clear();
    }
}
