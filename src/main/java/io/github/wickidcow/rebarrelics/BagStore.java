package io.github.wickidcow.rebarrelics;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

/**
 * Versioned, per-player canonical backing store.
 *
 * All 27 slots are persisted even if a player is opening a lower-tier bag.
 * Inactive slots cannot be overwritten by a lower-tier UI.
 * No bag inventory is ever embedded in a tradable item.
 */
public final class BagStore {
    private static final int SCHEMA_VERSION = 1;
    private static final int MAX_ITEM_BYTES = 256 * 1024;
    private final Path directory;

    public BagStore(Path pluginDataFolder) {
        this.directory = pluginDataFolder.resolve("talisman-bags");
    }

    private Path path(UUID owner) {
        return directory.resolve(owner.toString() + ".yml");
    }

    public synchronized ItemStack[] load(UUID owner) throws IOException {
        ItemStack[] items = new ItemStack[BagRules.BACKING_SLOTS];
        Path file = path(owner);
        if (!Files.exists(file)) return items;

        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file.toFile());
        } catch (InvalidConfigurationException failure) {
            throw new IOException("Bag file is malformed: " + file, failure);
        }
        if (yaml.getInt("version", -1) != SCHEMA_VERSION
                || !owner.toString().equals(yaml.getString("owner"))) {
            throw new IOException("Bag storage version/owner mismatch: " + file);
        }
        ConfigurationSection slots = yaml.getConfigurationSection("slots");
        if (slots == null) return items;
        for (String index : slots.getKeys(false)) {
            int slot;
            try {
                slot = Integer.parseInt(index);
            } catch (NumberFormatException ex) {
                throw new IOException("Unknown bag slot: " + index, ex);
            }
            if (slot < 0 || slot >= BagRules.BACKING_SLOTS) {
                throw new IOException("Invalid bag slot index: " + slot);
            }
            String encoded = slots.getString(index);
            if (encoded == null || encoded.length() > MAX_ITEM_BYTES * 2) {
                throw new IOException("Invalid bag slot encoding: " + index);
            }
            try {
                byte[] bytes = Base64.getDecoder().decode(encoded);
                if (bytes.length > MAX_ITEM_BYTES) throw new IOException("Bag item too large: " + slot);
                ItemStack stack = ItemStack.deserializeBytes(bytes);
                if (!RelicItems.isStorableTalisman(stack)) {
                    throw new IOException("Unrecognized or forbidden bag item in slot " + slot);
                }
                items[slot] = stack;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                throw new IOException("Unable to deserialize bag slot " + slot, ex);
            }
        }
        return items;
    }

    public synchronized void save(UUID owner, ItemStack[] items) throws IOException {
        if (items.length != BagRules.BACKING_SLOTS) {
            throw new IOException("Bag storage must have exactly 27 slots");
        }
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("version", SCHEMA_VERSION);
        yaml.set("owner", owner.toString());
        for (int slot = 0; slot < items.length; slot++) {
            ItemStack stack = items[slot];
            if (stack == null || stack.isEmpty()) continue;
            if (!RelicItems.isStorableTalisman(stack)) {
                throw new IOException("Refusing to save disallowed bag item in slot " + slot);
            }
            byte[] bytes = stack.serializeAsBytes();
            if (bytes.length > MAX_ITEM_BYTES) {
                throw new IOException("Bag item exceeds safe storage limit at slot " + slot);
            }
            yaml.set("slots." + slot, Base64.getEncoder().encodeToString(bytes));
        }

        Files.createDirectories(directory);
        Path file = path(owner);
        Path temp = Files.createTempFile(directory, ".talisman-bag-", ".tmp");
        try {
            Files.writeString(temp, yaml.saveToString(), StandardCharsets.UTF_8);
            // One backed-up snapshot aids recovery, but is never auto-replayed:
            // automatically restoring an older snapshot could duplicate withdrawn items.
            if (Files.exists(file)) {
                Files.copy(file, directory.resolve(owner + ".bak"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                throw new IOException("Bag storage requires an atomic filesystem rename", unsupported);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
