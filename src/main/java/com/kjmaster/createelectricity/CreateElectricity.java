package com.kjmaster.createelectricity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(CreateElectricity.ID)
public class CreateElectricity {

    public static final String ID = "create_electricity";
    public static final String NAME = "Create: Electricity";

    public static final Logger LOGGER = LogUtils.getLogger();

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    public CreateElectricity(IEventBus eventBus, ModContainer modContainer) {
        onCtor(eventBus, modContainer);
    }

    public static void onCtor(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("{} {} initializing! Commit hash: {}", NAME, CreateElectricityBuildInfo.VERSION, CreateElectricityBuildInfo.GIT_COMMIT);

        ModLoadingContext modLoadingContext = ModLoadingContext.get();

        REGISTRATE.registerEventListeners(modEventBus);

        AllCreativeModeTabs.register(modEventBus);
        AllBlocks.register();
        AllItems.register();
        AllBlockEntityTypes.register();
        AllRecipeTypes.register(modEventBus);
        AllDataComponents.register(modEventBus);

        modEventBus.addListener(CreateElectricity::init);
        modEventBus.addListener(CreateElectricity::onRegister);
    }

    public static void init(final FMLCommonSetupEvent event) {

    }

    public static void onRegister(final RegisterEvent event) {

    }

    public static LangBuilder lang() {
        return new LangBuilder(ID);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public static CreateRegistrate registrate() {
        if (!STACK_WALKER.getCallerClass().getPackageName().startsWith("com.kjmaster.createelectricity"))
            throw new UnsupportedOperationException("Other mods are not permitted to use create electricity's registrate instance.");
        return REGISTRATE;
    }
}
