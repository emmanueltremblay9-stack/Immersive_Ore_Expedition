package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;

import java.util.Objects;
import java.util.Optional;

/**
 * Registry-only bridge to ExtendedAE. IOE does not copy or compile against ExtendedAE classes.
 */
public final class ExtendedAeGeodeIntegration {
    public static final String MOD_ID = "extendedae";
    private static final ResourceRef BUDDING_ENTROIZED_FLUIX =
            ResourceRef.block(MOD_ID, "entro_budding_fully");
    private static final ResourceRef FLUIX_BLOCK = ResourceRef.block("ae2", "fluix_block");

    private ExtendedAeGeodeIntegration() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static Optional<GeodeMaterial> resolve() {
        if (!isLoaded()) {
            return Optional.empty();
        }
        Optional<Block> budding = BuiltInRegistries.BLOCK.getOptional(BUDDING_ENTROIZED_FLUIX.id());
        Optional<Block> fluix = BuiltInRegistries.BLOCK.getOptional(FLUIX_BLOCK.id());
        if (budding.isEmpty() || fluix.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new GeodeMaterial(
                BUDDING_ENTROIZED_FLUIX,
                budding.orElseThrow(),
                FLUIX_BLOCK,
                fluix.orElseThrow()
        ));
    }

    public record GeodeMaterial(
            ResourceRef buddingResource,
            Block buddingBlock,
            ResourceRef shellResource,
            Block shellBlock
    ) {
        public GeodeMaterial {
            Objects.requireNonNull(buddingResource, "buddingResource");
            Objects.requireNonNull(buddingBlock, "buddingBlock");
            Objects.requireNonNull(shellResource, "shellResource");
            Objects.requireNonNull(shellBlock, "shellBlock");
        }
    }
}
