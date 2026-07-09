package com.oblixorprime.ioe.expeditioncompass;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ExpeditionCompassResourceTest {
    private static final String ANGLE_PREDICATE = "immersive_ore_expedition:angle";
    private static final String MODEL_PREFIX = "immersive_ore_expedition:item/";
    private static final String FRAME_PREFIX = "expedition_compass_";
    private static final Set<Float> SAFE_BLOCK_MODEL_ROTATIONS = Set.of(-45.0F, -22.5F, 0.0F, 22.5F, 45.0F);
    private static final Set<String> REQUIRED_MENU_LANG_KEYS = Set.of(
            "item.immersive_ore_expedition.expedition_compass.message.invalid_target",
            "screen.immersive_ore_expedition.expedition_compass.title",
            "screen.immersive_ore_expedition.expedition_compass.dimension",
            "screen.immersive_ore_expedition.expedition_compass.current_target",
            "screen.immersive_ore_expedition.expedition_compass.no_current_target",
            "screen.immersive_ore_expedition.expedition_compass.empty",
            "screen.immersive_ore_expedition.expedition_compass.empty_worldgen_disabled",
            "screen.immersive_ore_expedition.expedition_compass.empty_proof_feature_disabled",
            "screen.immersive_ore_expedition.expedition_compass.empty_only_diagnostic",
            "screen.immersive_ore_expedition.expedition_compass.entry_title",
            "screen.immersive_ore_expedition.expedition_compass.entry_title_diagnostic",
            "screen.immersive_ore_expedition.expedition_compass.entry_detail",
            "screen.immersive_ore_expedition.expedition_compass.more_entries",
            "screen.immersive_ore_expedition.expedition_compass.showing_entries",
            "screen.immersive_ore_expedition.expedition_compass.previous",
            "screen.immersive_ore_expedition.expedition_compass.next",
            "screen.immersive_ore_expedition.expedition_compass.select",
            "screen.immersive_ore_expedition.expedition_compass.journeymap",
            "screen.immersive_ore_expedition.expedition_compass.journeymap_short",
            "screen.immersive_ore_expedition.expedition_compass.journeymap_created",
            "screen.immersive_ore_expedition.expedition_compass.journeymap_not_playable",
            "screen.immersive_ore_expedition.expedition_compass.journeymap_not_loaded",
            "screen.immersive_ore_expedition.expedition_compass.journeymap_not_ready",
            "screen.immersive_ore_expedition.expedition_compass.journeymap_failed",
            "screen.immersive_ore_expedition.expedition_compass.refresh",
            "screen.immersive_ore_expedition.expedition_compass.clear"
    );

    @Test
    void expeditionCompassModelUsesNamespacedAnglePredicateAndExistingFrameModels() throws IOException {
        JsonObject model = readJson(modelPath("expedition_compass"));
        JsonArray overrides = model.getAsJsonArray("overrides");
        assertNotNull(overrides);
        assertEquals(33, overrides.size());

        Set<String> referencedFrames = new HashSet<>();
        for (int i = 0; i < overrides.size(); i++) {
            JsonObject override = overrides.get(i).getAsJsonObject();
            JsonObject predicate = override.getAsJsonObject("predicate");
            assertNotNull(predicate);
            assertTrue(predicate.has(ANGLE_PREDICATE));
            assertEquals(expectedThreshold(i), predicate.get(ANGLE_PREDICATE).getAsFloat(), 0.000001F);

            String modelRef = override.get("model").getAsString();
            assertTrue(modelRef.startsWith(MODEL_PREFIX));

            String frameName = modelRef.substring(MODEL_PREFIX.length());
            assertTrue(frameName.startsWith(FRAME_PREFIX));
            referencedFrames.add(frameName);
            assertEquals(expectedFrameName(i), frameName);

            Path framePath = modelPath(frameName);
            assertTrue(Files.isRegularFile(framePath), () -> "Missing compass frame model " + framePath);
            assertFalse(readJson(framePath).has("overrides"));
        }

        assertEquals(32, referencedFrames.size());
        assertTrue(referencedFrames.contains("expedition_compass_00"));
        assertTrue(referencedFrames.contains("expedition_compass_31"));
    }

    @Test
    void expeditionCompassModelsReferenceExistingTexturesAndSafeRotations() throws IOException {
        for (Path modelPath : compassModelPaths()) {
            JsonObject model = readJson(modelPath);
            assertTexturesExist(model);
            assertRotationsAreModelLoaderSafe(model, modelPath);
        }
    }

    @Test
    void expeditionCompassMenuTranslationsExistInSupportedLocales() throws IOException {
        for (String locale : List.of("en_us", "fr_fr")) {
            JsonObject lang = readJson(langPath(locale));
            for (String key : REQUIRED_MENU_LANG_KEYS) {
                assertTrue(lang.has(key), () -> "Missing " + key + " in " + locale);
            }
        }
    }

    private static float expectedThreshold(int overrideIndex) {
        if (overrideIndex == 0) {
            return 0.0F;
        }
        return (overrideIndex - 0.5F) / 32.0F;
    }

    private static String expectedFrameName(int overrideIndex) {
        if (overrideIndex == 0 || overrideIndex == 32) {
            return "expedition_compass_00";
        }
        return "expedition_compass_" + String.format("%02d", overrideIndex);
    }

    private static void assertTexturesExist(JsonObject model) {
        JsonObject textures = model.getAsJsonObject("textures");
        assertNotNull(textures);
        for (String key : textures.keySet()) {
            String texture = textures.get(key).getAsString();
            Path path = texturePath(texture);
            assertTrue(Files.isRegularFile(path), () -> "Missing texture " + texture + " at " + path);
        }
    }

    private static void assertRotationsAreModelLoaderSafe(JsonObject model, Path path) {
        JsonArray elements = model.getAsJsonArray("elements");
        assertNotNull(elements);
        for (JsonElement element : elements) {
            JsonObject elementObject = element.getAsJsonObject();
            if (!elementObject.has("rotation")) {
                continue;
            }
            float angle = elementObject.getAsJsonObject("rotation").get("angle").getAsFloat();
            assertTrue(
                    SAFE_BLOCK_MODEL_ROTATIONS.contains(angle),
                    () -> "Unsafe block-model rotation " + angle + " in " + path
            );
        }
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static Path modelPath(String modelName) {
        return resourcesRoot()
                .resolve("assets")
                .resolve("immersive_ore_expedition")
                .resolve("models")
                .resolve("item")
                .resolve(modelName + ".json");
    }

    private static Path langPath(String locale) {
        return resourcesRoot()
                .resolve("assets")
                .resolve("immersive_ore_expedition")
                .resolve("lang")
                .resolve(locale + ".json");
    }

    private static List<Path> compassModelPaths() throws IOException {
        Path modelRoot = resourcesRoot()
                .resolve("assets")
                .resolve("immersive_ore_expedition")
                .resolve("models")
                .resolve("item");
        try (var stream = Files.list(modelRoot)) {
            return stream
                    .filter(path -> path.getFileName().toString().matches("expedition_compass(_\\d{2})?\\.json"))
                    .sorted()
                    .toList();
        }
    }

    private static Path texturePath(String texture) {
        String[] parts = texture.split(":", 2);
        String namespace = parts.length == 2 ? parts[0] : "minecraft";
        String path = parts.length == 2 ? parts[1] : parts[0];
        return resourcesRoot()
                .resolve("assets")
                .resolve(namespace)
                .resolve("textures")
                .resolve(path + ".png");
    }

    private static Path resourcesRoot() {
        Path moduleRoot = Path.of("src", "main", "resources");
        if (Files.isDirectory(moduleRoot)) {
            return moduleRoot;
        }
        return Path.of("ioe_project_packs", "immersive_ore_expedition", "src", "main", "resources");
    }
}
