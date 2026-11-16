package io.github.wickidcow.rebarrelics;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import io.github.pylonmc.rebar.event.RebarConfigurableRecipesLoadedEvent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only runtime audit: validates that the recipes shipped in the JAR are
 * actually registered with the Paper recipe registry after Rebar loads all\n * configurable recipes (which happens after plugin startup, on the first tick).
 * No recipe mutation or automatic inventory repair is performed.
 */
public final class RecipeStartupAudit implements Listener {
    private final RebarRelics plugin;

    public RecipeStartupAudit(RebarRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRecipesLoaded(RebarConfigurableRecipesLoadedEvent event) {
        try (InputStream input = plugin.getResource("recipes/minecraft/crafting_shaped.yml")) {
            if (input == null) {
                plugin.getLogger().severe("Rebar Relics recipe registration incomplete: bundled file missing");
                return;
            }

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(input, StandardCharsets.UTF_8));
            List<String> missing = new ArrayList<>();
            int expected = yaml.getKeys(false).size();
            for (String id : yaml.getKeys(false)) {
                NamespacedKey key = NamespacedKey.fromString(id);
                if (key == null || Bukkit.getRecipe(key) == null) {
                    missing.add(id);
                }
            }
            plugin.getLogger().info("Recipe registration audit: "
                    + (expected - missing.size()) + "/" + expected);
            if (!missing.isEmpty()) {
                plugin.getLogger().severe("Rebar Relics recipe registration incomplete: "
                        + String.join(", ", missing));
            }
        } catch (Exception exception) {
            plugin.getLogger().severe("Rebar Relics recipe registration incomplete: "
                    + exception.getMessage());
        }
    }
}
