package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Registry-only bridge to GeOre. GeOre remains the owner of its implementation and assets;
 * IOE only selects registered materials and controls where they enter world generation.
 */
public final class GeOreNodeIntegration {
    public static final String MOD_ID = "geore";
    private static final Set<String> SUPPORTED_MATERIALS = Set.of(
            "aluminum",
            "coal",
            "copper",
            "diamond",
            "emerald",
            "gold",
            "iron",
            "lapis",
            "lead",
            "nickel",
            "redstone",
            "silver",
            "uranium"
    );

    private GeOreNodeIntegration() {
    }

    public static Optional<NodeMaterial> resolve(String materialName) {
        Objects.requireNonNull(materialName, "materialName");
        if (!SUPPORTED_MATERIALS.contains(materialName)) {
            return Optional.empty();
        }

        ResourceRef nodeResource = ResourceRef.block(MOD_ID, materialName + "_block");
        ResourceRef buddingResource = ResourceRef.block(MOD_ID, "budding_" + materialName);
        Optional<Block> nodeBlock = BuiltInRegistries.BLOCK.getOptional(nodeResource.id());
        Optional<Block> buddingBlock = BuiltInRegistries.BLOCK.getOptional(buddingResource.id());
        if (nodeBlock.isEmpty() || buddingBlock.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new NodeMaterial(
                nodeResource,
                nodeBlock.orElseThrow(),
                buddingResource,
                buddingBlock.orElseThrow()
        ));
    }

    public static boolean autonomousWorldgenFeature(ResourceLocation featureId) {
        Objects.requireNonNull(featureId, "featureId");
        return MOD_ID.equals(featureId.getNamespace())
                && featureId.getPath().endsWith("_geode");
    }

    public record NodeMaterial(
            ResourceRef nodeResource,
            Block nodeBlock,
            ResourceRef buddingResource,
            Block buddingBlock
    ) {
        public NodeMaterial {
            Objects.requireNonNull(nodeResource, "nodeResource");
            Objects.requireNonNull(nodeBlock, "nodeBlock");
            Objects.requireNonNull(buddingResource, "buddingResource");
            Objects.requireNonNull(buddingBlock, "buddingBlock");
        }
    }
}
