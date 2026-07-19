package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;

import java.util.Objects;
import java.util.Optional;

/**
 * Registry-only bridge to AE2. IOE does not copy or compile against AE2 implementation classes.
 */
public final class Ae2MeteoriteIntegration {
    public static final String MOD_ID = "ae2";
    public static final String CRYSTAL_SCIENCE_MOD_ID = "ae2cs";
    // Deliberately repairable: AE2 consumes ae2:charged_certus_quartz_crystal through its native water transforms.
    private static final ResourceRef BUDDING_CERTUS = ResourceRef.block(MOD_ID, "flawed_budding_quartz");
    private static final ResourceRef SKY_STONE = ResourceRef.block(MOD_ID, "sky_stone_block");

    private Ae2MeteoriteIntegration() {
    }

    public static boolean isCrystalProcessingStackLoaded() {
        return ModList.get().isLoaded(MOD_ID) && ModList.get().isLoaded(CRYSTAL_SCIENCE_MOD_ID);
    }

    public static Optional<MeteoriteMaterial> resolve() {
        if (!isCrystalProcessingStackLoaded()) {
            return Optional.empty();
        }
        Optional<Block> buddingCertus = BuiltInRegistries.BLOCK.getOptional(BUDDING_CERTUS.id());
        Optional<Block> skyStone = BuiltInRegistries.BLOCK.getOptional(SKY_STONE.id());
        if (buddingCertus.isEmpty() || skyStone.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new MeteoriteMaterial(
                BUDDING_CERTUS,
                buddingCertus.orElseThrow(),
                SKY_STONE,
                skyStone.orElseThrow()
        ));
    }

    public static boolean forbiddenCertusOre(ResourceLocation id) {
        Objects.requireNonNull(id, "id");
        if (!MOD_ID.equals(id.getNamespace())
                && !CRYSTAL_SCIENCE_MOD_ID.equals(id.getNamespace())
                && !"appeng".equals(id.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        return path.contains("certus") && path.contains("ore");
    }

    static boolean expeditionFormationBlock(ResourceLocation id) {
        return BUDDING_CERTUS.id().equals(id) || SKY_STONE.id().equals(id);
    }

    public record MeteoriteMaterial(
            ResourceRef buddingResource,
            Block buddingBlock,
            ResourceRef skyStoneResource,
            Block skyStoneBlock
    ) {
        public MeteoriteMaterial {
            Objects.requireNonNull(buddingResource, "buddingResource");
            Objects.requireNonNull(buddingBlock, "buddingBlock");
            Objects.requireNonNull(skyStoneResource, "skyStoneResource");
            Objects.requireNonNull(skyStoneBlock, "skyStoneBlock");
        }
    }
}
