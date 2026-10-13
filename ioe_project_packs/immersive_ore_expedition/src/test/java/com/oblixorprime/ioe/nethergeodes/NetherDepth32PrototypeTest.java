package com.oblixorprime.ioe.nethergeodes;

import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

final class NetherDepth32PrototypeTest {
    @TempDir Path directory;
    @Test void freshDirectoryAllowedButExistingWorldsRefusedWithoutMutation() throws Exception {
        Files.createDirectories(directory.resolve("mods"));
        Files.writeString(directory.resolve("mods/example.jar"),"not a world");
        assertDoesNotThrow(()->NetherDepth32Prototype.requireFreshDirectory(directory));
        for(var name:new String[]{"level.dat","level.dat_old","region/r.0.0.mca","DIM-1/region/r.-1.2.mca"}) {
            var file=directory.resolve("saved/"+name);Files.createDirectories(file.getParent());Files.writeString(file,"preserve");
            assertThrows(IllegalStateException.class,()->NetherDepth32Prototype.requireFreshDirectory(directory));
            assertEquals("preserve",Files.readString(file));Files.delete(file);
        }
    }
    private static com.google.gson.JsonObject data(String path) throws Exception {
        return com.google.gson.JsonParser.parseString(Files.readString(Path.of(System.getProperty("ioe.moduleProjectDir"),"prototype/depth32/"+path))).getAsJsonObject();
    }
    @Test void fixedUpperBoundLavaAndCarverAnchors() throws Exception {
        var dimension=data("data/minecraft/dimension_type/the_nether.json");
        assertEquals(-32,dimension.get("min_y").getAsInt());
        assertEquals(256,dimension.get("min_y").getAsInt()+dimension.get("height").getAsInt());
        assertEquals(128,dimension.get("min_y").getAsInt()+dimension.get("logical_height").getAsInt());
        var noise=data("data/minecraft/worldgen/noise_settings/nether.json");
        assertEquals(32,noise.get("sea_level").getAsInt());
        assertEquals(-32,noise.getAsJsonObject("noise").get("min_y").getAsInt());
        assertEquals(160,noise.getAsJsonObject("noise").get("height").getAsInt());
        var carver=data("data/minecraft/worldgen/configured_carver/nether_cave.json").getAsJsonObject("config");
        assertEquals(10,carver.getAsJsonObject("lava_level").get("absolute").getAsInt());
        assertEquals(0,carver.getAsJsonObject("y").getAsJsonObject("min_inclusive").get("absolute").getAsInt());
        assertEquals(126,carver.getAsJsonObject("y").getAsJsonObject("max_inclusive").get("absolute").getAsInt());
    }
    @Test void clonedFeaturesRetainSuppressionAndCannotOverrideOverworldFeatures() throws Exception {
        var original=com.google.gson.JsonParser.parseString(Files.readString(Path.of(System.getProperty("ioe.moduleProjectDir"),"src/main/resources/data/immersive_ore_expedition/tags/worldgen/placed_feature/normal_ore_generation.json"))).getAsJsonObject().getAsJsonArray("values");
        var extra=data("data/immersive_ore_expedition/tags/worldgen/placed_feature/normal_ore_generation.json").getAsJsonArray("values");
        assertFalse(Files.exists(Path.of(System.getProperty("ioe.moduleProjectDir"),"prototype/depth32/data/minecraft/worldgen/placed_feature")));
        for(var entry:data("PROVENANCE.json").getAsJsonArray("cloned_nether_features")) {
            var name=entry.getAsString();
            assertFalse(data("data/ioe_depth32/worldgen/placed_feature/"+name+".json").toString().contains("above_bottom"));
            assertFalse(data("data/ioe_depth32/worldgen/placed_feature/"+name+".json").toString().contains("below_top"));
            if(original.contains(new com.google.gson.JsonPrimitive("minecraft:"+name)))
                assertTrue(extra.contains(new com.google.gson.JsonPrimitive("ioe_depth32:"+name)),name);
        }
        assertFalse(Files.exists(Path.of(System.getProperty("ioe.moduleProjectDir"),"src/main/resources/data/minecraft/dimension_type/the_nether.json")));
    }
}
