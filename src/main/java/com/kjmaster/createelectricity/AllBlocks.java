package com.kjmaster.createelectricity;

import com.simibubi.create.foundation.data.CreateRegistrate;

public class AllBlocks {

    private static final CreateRegistrate REGISTRATE = CreateElectricity.registrate();

    static {
        REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB);
    }

    public static void register() {
    }
}
