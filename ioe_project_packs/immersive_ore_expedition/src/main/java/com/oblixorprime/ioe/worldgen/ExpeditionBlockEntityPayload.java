package com.oblixorprime.ioe.worldgen;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Minimal typed payload needed by expedition loot containers and materialized Domum accents. */
public record ExpeditionBlockEntityPayload(
        ResourceLocation lootTable,
        long lootSeed,
        Map<ResourceLocation, ResourceLocation> materialBlocks
) {
    public ExpeditionBlockEntityPayload {
        materialBlocks = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNull(materialBlocks, "materialBlocks")));
        if (lootTable == null && materialBlocks.isEmpty()) {
            throw new IllegalArgumentException("A block-entity payload must configure loot or material data");
        }
        if (lootTable != null && !materialBlocks.isEmpty()) {
            throw new IllegalArgumentException("Loot and Domum material payloads must use separate block entities");
        }
    }

    static ExpeditionBlockEntityPayload loot(ResourceLocation lootTable, long lootSeed) {
        return new ExpeditionBlockEntityPayload(Objects.requireNonNull(lootTable, "lootTable"), lootSeed, Map.of());
    }

    static ExpeditionBlockEntityPayload materials(Map<ResourceLocation, ResourceLocation> materialBlocks) {
        return new ExpeditionBlockEntityPayload(null, 0L, materialBlocks);
    }

    boolean hasLootTable() {
        return lootTable != null;
    }

    boolean hasMaterialBlocks() {
        return !materialBlocks.isEmpty();
    }
}
