package com.kjmaster.createelectricity.foundation.events;

import com.kjmaster.createelectricity.content.kinetics.dynamo.MagneticDynamoBlockEntity;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

//@EventBusSubscriber
public class CommonEvents {


    @EventBusSubscriber
    public static class ModBusEvents {

        @net.neoforged.bus.api.SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            MagneticDynamoBlockEntity.registerCapabilities(event);
        }
    }
}
