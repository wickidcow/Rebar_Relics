package io.github.wickidcow.rebarrelics;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Validate hard-mode native Rebar recipe definitions without a running Paper server. */
class RecipeContractTest {
    @SuppressWarnings("unchecked")
    private static Map<String, Object> load(String resource) {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        try (InputStream in = RecipeContractTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, "Missing resource " + resource);
            return (Map<String, Object>) new Yaml(new SafeConstructor(options)).load(in);
        } catch (java.io.IOException exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void exactlyOneHardRecipePerActiveItem() {
        Map<String, Object> recipes = load("recipes/minecraft/crafting_shaped.yml");
        Map<String, Object> lang = (Map<String, Object>) load("lang/en.yml").get("item");
        assertEquals(33, recipes.size(), "29 active relics and four crafting materials");
        for (Map.Entry<String, Object> entry : recipes.entrySet()) {
            assertTrue(entry.getKey().startsWith("rebar_relics:"));
            String id = entry.getKey().substring("rebar_relics:".length());
            assertTrue(lang.containsKey(id), "Missing translation for " + id);
            Map<String, Object> recipe = (Map<String, Object>) entry.getValue();
            assertEquals(entry.getKey(), recipe.get("result"));
            List<String> shape = (List<String>) recipe.get("pattern");
            assertEquals(3, shape.size(), id);
            Map<String, String> key = (Map<String, String>) recipe.get("key");
            assertEquals('K', shape.get(1).charAt(1),
                    "All recipes use a central progression material");
            for (String row : shape) {
                assertEquals(3, row.length(), id);
                for (char symbol : row.toCharArray()) {
                    assertTrue(key.containsKey(String.valueOf(symbol)), id + " missing " + symbol);
                }
            }
            assertTrue(key.get("K").startsWith("minecraft:")
                    || key.get("K").startsWith("rebar_relics:"), "Unrecognized center for " + id);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void upgradesCostEightLowerTierItemsAndOneRareCore() {
        Map<String, Object> recipes = load("recipes/minecraft/crafting_shaped.yml");
        for (String family : List.of("archery", "strength", "attack_speed", "boss", "poseidon", "resistance")) {
            for (int tier = 2; tier <= 3; tier++) {
                String id = "rebar_relics:" + family + "_talisman_" + tier;
                Map<String, Object> recipe = (Map<String, Object>) recipes.get(id);
                assertNotNull(recipe, id);
                assertEquals(List.of("PPP", "PKP", "PPP"), recipe.get("pattern"));
                Map<String, String> key = (Map<String, String>) recipe.get("key");
                assertEquals("rebar_relics:" + family + "_talisman_" + (tier - 1), key.get("P"));
                assertEquals("rebar_relics:talisman_core_" + tier, key.get("K"));
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void noNewCopiesOfPylonTalismanFamilies() {
        Map<String, Object> recipes = load("recipes/minecraft/crafting_shaped.yml");
        Set<String> duplicates = Set.of(
                "experience_talisman_1", "water_breather_talisman_1",
                "health_talisman_1", "farming_talisman_1", "hunting_talisman_1",
                "luck_talisman_1", "breeding_talisman_1", "enchanting_talisman_1");
        for (String id : duplicates) {
            assertFalse(recipes.containsKey("rebar_relics:" + id), "Do not clone Pylon " + id);
        }

        Map<String, Object> config = load("config.yml");
        Map<String, Object> talismans = (Map<String, Object>) config.get("talismans");
        assertEquals(false, ((Map<String, Object>) talismans.get("experience")).get("enabled"));
        assertEquals(false, ((Map<String, Object>) talismans.get("water-breather")).get("enabled"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void allCoresUsePylonMaterials() {
        Map<String, Object> recipes = load("recipes/minecraft/crafting_shaped.yml");
        for (String id : List.of("relic_matrix", "talisman_core_1", "talisman_core_2", "talisman_core_3")) {
            Map<String, Object> recipe = (Map<String, Object>) recipes.get("rebar_relics:" + id);
            assertNotNull(recipe);
            Map<String, String> key = (Map<String, String>) recipe.get("key");
            assertTrue(key.values().stream().anyMatch(s -> s.startsWith("pylon:")), id);
        }
    }
}
