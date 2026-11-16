package io.github.wickidcow.rebarrelics;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Versioned per-player talisman storage. Never stored inside tradeable bag keys.
 *
 * Version 2 protects against *accidental file corruption*, including a
 * syntactically valid YAML file with silently missing slots. It is not an
 * authentication code and cannot protect against malicious filesystem editors.
 * Version 1 is read-only compatible and migrates on the next successful save.
 *
 * Disk writes are atomic, but cannot be an atomic transaction with the Paper
 * player's inventory. Keep the bag opt-in until crash-window tests are complete.
 */
public final class BagStore {
    private static final int SCHEMA_VERSION = 2;
    private static final int LEGACY_VERSION = 1;
    private static final int MAX_ITEM_BYTES = 256 * 1024;
    private static final long MAX_FILE_BYTES = 12L * 1024 * 1024;
    private final Path directory;

    public BagStore(Path pluginDataFolder) {
        directory = pluginDataFolder.resolve("talisman-bags");
    }

    private Path path(UUID owner) {
        return directory.resolve(owner + ".yml");
    }

    private static String checksum(UUID owner, String[] encoded) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(("rebar-relics-bag-v2\n" + owner + "\n")
                    .getBytes(StandardCharsets.UTF_8));
            for (int slot = 0; slot < BagRules.BACKING_SLOTS; slot++) {
                digest.update((slot + ":" + (encoded[slot] == null ? "-" : encoded[slot])
                        + "\n").getBytes(StandardCharsets.UTF_8));
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossibleOnJdk25) {
            throw new IllegalStateException("Java SHA-256 support unavailable", impossibleOnJdk25);
        }
    }

    public synchronized ItemStack[] load(UUID owner) throws IOException {
        ItemStack[] items = new ItemStack[BagRules.BACKING_SLOTS];
        Path file = path(owner);
        if (!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) return items;
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)
                || Files.size(file) > MAX_FILE_BYTES) {
            throw new IOException("Bag file is not a regular, reasonably sized file: " + file);
        }

        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file.toFile());
        } catch (InvalidConfigurationException failure) {
            throw new IOException("Bag file is malformed: " + file, failure);
        }
        int version = yaml.getInt("version", -1);
        if ((version != SCHEMA_VERSION && version != LEGACY_VERSION)
                || !owner.toString().equals(yaml.getString("owner"))) {
            throw new IOException("Bag storage version/owner mismatch: " + file);
        }

        ConfigurationSection slots = yaml.getConfigurationSection("slots");
        if (slots == null && yaml.contains("slots")) {
            throw new IOException("Bag slots have an invalid YAML type: " + file);
        }

        String[] encoded = new String[BagRules.BACKING_SLOTS];
        if (slots != null) {
            for (String index : slots.getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(index);
                } catch (NumberFormatException ex) {
                    throw new IOException("Unknown bag slot: " + index, ex);
                }
                if (slot < 0 || slot >= BagRules.BACKING_SLOTS
                        || !Integer.toString(slot).equals(index)) {
                    throw new IOException("Invalid bag slot index: " + index);
                }
                String value = slots.getString(index);
                if (value == null || value.isEmpty() || value.length() > MAX_ITEM_BYTES * 2) {
                    throw new IOException("Invalid bag slot encoding: " + index);
                }
                encoded[slot] = value;
            }
        }

        if (version == SCHEMA_VERSION) {
            String expected = checksum(owner, encoded);
            String stored = yaml.getString("sha256");
            if (stored == null || !stored.equals(expected)) {
                throw new IOException("Bag storage checksum mismatch; refusing unsafe recovery: "
                        + file + ". Back up the file and contact an administrator.");
            }
        }

        for (int slot = 0; slot < BagRules.BACKING_SLOTS; slot++) {
            if (encoded[slot] == null) continue;
            try {
                byte[] bytes = Base64.getDecoder().decode(encoded[slot]);
                if (bytes.length > MAX_ITEM_BYTES) {
                    throw new IOException("Bag item too large: " + slot);
                }
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
        if (items == null || items.length != BagRules.BACKING_SLOTS) {
            throw new IOException("Bag storage must have exactly 27 slots");
        }
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("version", SCHEMA_VERSION);
        yaml.set("owner", owner.toString());
        String[] encoded = new String[BagRules.BACKING_SLOTS];
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
            encoded[slot] = Base64.getEncoder().encodeToString(bytes);
            yaml.set("slots." + slot, encoded[slot]);
        }
        yaml.set("sha256", checksum(owner, encoded));

        String contents = yaml.saveToString();
        if (contents.getBytes(StandardCharsets.UTF_8).length > MAX_FILE_BYTES) {
            throw new IOException("Bag contents exceed file size limit");
        }

        Files.createDirectories(directory);
        Path file = path(owner);
        Path temp = Files.createTempFile(directory, ".talisman-bag-", ".tmp");
        try {
            Files.writeString(temp, contents, StandardCharsets.UTF_8);
            // One backed-up snapshot aids manual recovery. It is never
            // restored automatically: that could duplicate withdrawn items.
            if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
                if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                    throw new IOException("Refusing to replace non-regular bag storage file");
                }
                Files.copy(file, directory.resolve(owner + ".bak"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                throw new IOException("Bag storage requires atomic filesystem rename", unsupported);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
