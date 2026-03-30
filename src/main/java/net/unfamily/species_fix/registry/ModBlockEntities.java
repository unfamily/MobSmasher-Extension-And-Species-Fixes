package net.unfamily.species_fix.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.unfamily.species_fix.SpeciesFixes;
import net.unfamily.species_fix.block.SoulInhibitorBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SpeciesFixes.MODID);

    @SuppressWarnings("null")
    public static final RegistryObject<BlockEntityType<SoulInhibitorBlockEntity>> SOUL_INHIBITOR =
            BLOCK_ENTITIES.register("soul_inhibitor", () ->
                    BlockEntityType.Builder.of(SoulInhibitorBlockEntity::new, ModBlocks.SOUL_INHIBITOR.get())
                            .build(null));

    private ModBlockEntities() {}
}
