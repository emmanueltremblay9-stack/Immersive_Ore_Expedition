package com.oblixorprime.ioe.budding;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

/** Original generation metadata; the current rank always comes from the live block. */
public record BuddingNodeInfo(BlockPos pos, int index, int count, int initialOre, ResourceLocation family) {
    public BuddingNodeInfo {
        pos = Objects.requireNonNull(pos, "pos").immutable();
        Objects.requireNonNull(family, "family");
        if (index < 1 || index > count || count > 7 || initialOre < 4 || initialOre > 7) {
            throw new IllegalArgumentException("Invalid canonical node metadata");
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("pos", pos.asLong());
        tag.putInt("index", index);
        tag.putInt("count", count);
        tag.putInt("ore", initialOre);
        tag.putString("family", family.toString());
        return tag;
    }

    public static Optional<BuddingNodeInfo> load(CompoundTag tag) {
        ResourceLocation family = ResourceLocation.tryParse(tag.getString("family"));
        if (family == null || !tag.contains("pos", Tag.TAG_LONG)) return Optional.empty();
        try {
            return Optional.of(new BuddingNodeInfo(BlockPos.of(tag.getLong("pos")), tag.getInt("index"),
                    tag.getInt("count"), tag.getInt("ore"), family));
        } catch (IllegalArgumentException invalid) {
            return Optional.empty();
        }
    }
}
