package com.oblixorprime.ioe.core;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IoeCoreMod {
    public static final String MODID = ImmersiveOreExpeditionMod.MODID;
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Core");
    public static final ResourcePolicyService RESOURCE_POLICY = new ResourcePolicyService();

    private IoeCoreMod() {
    }

    public static void bootstrap() {
        LOGGER.info("Initializing Immersive Ore Expedition: Core resource policy services");
    }
}
