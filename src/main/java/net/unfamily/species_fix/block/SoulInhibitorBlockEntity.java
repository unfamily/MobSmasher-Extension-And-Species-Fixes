package net.unfamily.species_fix.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.unfamily.species_fix.SoulInhibitorChunkIndex;
import net.unfamily.species_fix.registry.ModBlockEntities;

public class SoulInhibitorBlockEntity extends BlockEntity {
    private static final String TAG_MODE = "SoulRedstoneMode";

    private SoulInhibitorRedstoneMode redstoneMode = SoulInhibitorRedstoneMode.PULSE;

    public SoulInhibitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOUL_INHIBITOR.get(), pos, state);
    }

    public SoulInhibitorRedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void cycleRedstoneMode() {
        redstoneMode = redstoneMode.next();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte(TAG_MODE, (byte) redstoneMode.ordinal());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        redstoneMode = SoulInhibitorRedstoneMode.fromOrdinal(
                tag.contains(TAG_MODE) ? tag.getByte(TAG_MODE) : SoulInhibitorRedstoneMode.PULSE.ordinal());
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide() && level instanceof ServerLevel sl && level.getServer() != null) {
            level.getServer().execute(() -> {
                if (isRemoved()) return;
                SoulInhibitorBlock.applyModeAndRedstone(sl, worldPosition);
            });
        }
    }
}
