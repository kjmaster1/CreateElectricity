package com.kjmaster.createelectricity;

import com.kjmaster.createelectricity.api.network.WireType;
import com.kjmaster.createelectricity.content.items.WireSpoolItem;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.recipe.CommonMetal;
import com.simibubi.create.foundation.item.TagDependentIngredientItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static com.simibubi.create.AllTags.AllItemTags.CRUSHED_RAW_MATERIALS;

public class AllItems {

    private static final CreateRegistrate REGISTRATE = CreateElectricity.registrate();

    static {
        REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB);
    }

    public static final ItemEntry<WireSpoolItem> COPPER_WIRE_SPOOL = REGISTRATE
            .item("copper_wire_spool", props -> new WireSpoolItem(props, WireType.COPPER, 16.0f))
            .register();

    private static ItemEntry<Item> ingredient(String name) {
        return REGISTRATE.item(name, Item::new)
                .register();
    }

    private static ItemEntry<SequencedAssemblyItem> sequencedIngredient(String name) {
        return REGISTRATE.item(name, SequencedAssemblyItem::new)
                .register();
    }

    @SafeVarargs
    private static ItemEntry<Item> taggedIngredient(String name, TagKey<Item>... tags) {
        return REGISTRATE.item(name, Item::new)
                .tag(tags)
                .register();
    }

    private static ItemEntry<TagDependentIngredientItem> compatCrushedOre(CommonMetal metal) {
        return REGISTRATE
                .item("crushed_raw_" + metal,
                        props -> new TagDependentIngredientItem(props, metal.ores.items()))
                .tag(CRUSHED_RAW_MATERIALS.tag)
                .register();
    }

    // Load this class

    public static void register() {
    }
}
