package com.oblixorprime.ioe.core;

import net.minecraft.resources.ResourceLocation;

public final class ResourcePolicyService {
    public boolean isApprovedResource(ResourceLocation id) {
        // TODO Codex: implement whitelist/tag validation from server config.
        return false;
    }

    public boolean shouldSkipMissing(ResourceLocation id) {
        return true;
    }
}
