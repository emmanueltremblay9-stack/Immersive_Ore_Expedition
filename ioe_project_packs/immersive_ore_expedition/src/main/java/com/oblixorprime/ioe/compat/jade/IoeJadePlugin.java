package com.oblixorprime.ioe.compat.jade;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.budding.IronBuddingBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(ImmersiveOreExpeditionMod.MODID)
public final class IoeJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(IoeBuddingProvider.INSTANCE, IronBuddingBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(IoeBuddingProvider.INSTANCE, IronBuddingBlock.class);
    }
}
