package com.kjmaster.createelectricity;

import com.kjmaster.createelectricity.content.infrastructure.CeramicInsulatorBlockEntity;
import com.kjmaster.createelectricity.content.kinetics.dynamo.MagneticDynamoBlockEntity;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class AllBlockEntityTypes {
    private static final CreateRegistrate REGISTRATE = CreateElectricity.registrate();

    public static final BlockEntityEntry<MagneticDynamoBlockEntity> MAGNETIC_DYNAMO = REGISTRATE
            .blockEntity("magnetic_dynamo", MagneticDynamoBlockEntity::new)
            .validBlocks(AllBlocks.MAGNETIC_DYNAMO)
            .register();

    public static final BlockEntityEntry<CeramicInsulatorBlockEntity> CERAMIC_INSULATOR = REGISTRATE
            .blockEntity("ceramic_insulator", CeramicInsulatorBlockEntity::new)
            .validBlocks(AllBlocks.CERAMIC_INSULATOR)
            .register();

    public static void register() {
    }
}
