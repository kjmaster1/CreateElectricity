package com.kjmaster.createelectricity.api.capability;

import com.kjmaster.createelectricity.CreateElectricity;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;

public class ElectrodynamicCapabilities {

    public static final BlockCapability<IElectrodynamicPower, Direction> POWER =
            BlockCapability.createSided(
                    CreateElectricity.asResource("electrodynamic_power"),
                    IElectrodynamicPower.class
            );
}
