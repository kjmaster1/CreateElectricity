package com.kjmaster.createelectricity;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = CreateElectricity.ID, dist = Dist.CLIENT)
public class CreateElectricityClient {
    public CreateElectricityClient(IEventBus modEventBus) {
        onCtorClient(modEventBus);
    }

    public static void onCtorClient(IEventBus modEventBus) {
        modEventBus.addListener(CreateElectricityClient::clientInit);
    }

    public static void clientInit(final FMLClientSetupEvent event) {

        setupConfigUIBackground();
    }

    private static void setupConfigUIBackground() {

    }


}
