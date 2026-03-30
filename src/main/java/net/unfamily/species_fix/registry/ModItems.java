package net.unfamily.species_fix.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.unfamily.species_fix.SpeciesFixes;

@Mod.EventBusSubscriber(modid = SpeciesFixes.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SpeciesFixes.MODID);

    @SuppressWarnings("null")
    public static final RegistryObject<Item> SOUL_INHIBITOR = ITEMS.register("soul_inhibitor", () ->
            new BlockItem(ModBlocks.SOUL_INHIBITOR.get(), new Item.Properties()));

    @SubscribeEvent
    public static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(SOUL_INHIBITOR);
        }
    }

    private ModItems() {}
}
