package com.kjmaster.createelectricity.foundation.events;

import com.kjmaster.createelectricity.api.network.ElectricalGridManager;
import com.kjmaster.createelectricity.content.kinetics.dynamo.MagneticDynamoBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber
public class CommonEvents {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            ElectricalGridManager.get(serverLevel).tick();
        }
    }

    @EventBusSubscriber
    public static class ModBusEvents {

        @net.neoforged.bus.api.SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            MagneticDynamoBlockEntity.registerCapabilities(event);
        }
    }
}
