package com.kjmaster.createelectricity;

import com.kjmaster.createelectricity.content.kinetics.dynamo.MagneticDynamoBlock;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;

public class AllBlocks {

    private static final CreateRegistrate REGISTRATE = CreateElectricity.registrate();

    static {
        REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB);
    }

    public static final BlockEntry<MagneticDynamoBlock> MAGNETIC_DYNAMO = REGISTRATE.block("magnetic_dynamo", MagneticDynamoBlock::new)
            .initialProperties(SharedProperties::stone)
            .item().build()
            .register();

    public static void register() {
    }
}
