package net.unfamily.species_fix.registry;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.unfamily.species_fix.SpeciesFixes;
import net.unfamily.species_fix.block.SoulInhibitorBlock;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SpeciesFixes.MODID);

    @SuppressWarnings("null")
    public static final RegistryObject<Block> SOUL_INHIBITOR = BLOCKS.register("soul_inhibitor", SoulInhibitorBlock::new);

    private ModBlocks() {}
}
