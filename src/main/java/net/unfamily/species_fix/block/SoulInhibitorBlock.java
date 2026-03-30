package net.unfamily.species_fix.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.unfamily.species_fix.Config;
import net.unfamily.species_fix.SoulInhibitorChunkIndex;

import javax.annotation.Nullable;

public class SoulInhibitorBlock extends Block implements EntityBlock {
    public static final BooleanProperty ON = BooleanProperty.create("on");
    private static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    private static final VoxelShape SHAPE_NORTH = Block.box(1, 2, 14, 15, 14, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(1, 2, 0, 15, 14, 2);
    private static final VoxelShape SHAPE_WEST = Block.box(14, 2, 1, 16, 14, 15);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 2, 1, 2, 14, 15);

    @SuppressWarnings("null")
    public SoulInhibitorBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(3.5F, 1200.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ON, true)
                .setValue(POWERED, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    @SuppressWarnings("null")
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ON, POWERED, FACING);
    }

    @Override
    @SuppressWarnings("null")
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        Direction facing = context.getHorizontalDirection();
        Direction facingTowardsPlayer = facing.getOpposite();
        return this.defaultBlockState()
                .setValue(POWERED, powered)
                .setValue(FACING, facingTowardsPlayer);
    }

    @Override
    @SuppressWarnings("null")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!Config.soulInhibitorEnabled) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SoulInhibitorBlockEntity tile)) return InteractionResult.PASS;
        ServerLevel sl = (ServerLevel) level;

        if (player.isShiftKeyDown()) {
            tile.cycleRedstoneMode();
            player.displayClientMessage(Component.translatable(tile.getRedstoneMode().translationKey()), true);
            applyModeAndRedstone(sl, pos);
            return InteractionResult.CONSUME;
        }

        BlockState newState = state.cycle(ON);
        level.setBlock(pos, newState, Block.UPDATE_ALL);
        SoulInhibitorChunkIndex.refresh(sl, pos);
        boolean nowOn = newState.getValue(ON);
        player.displayClientMessage(Component.translatable(
                nowOn ? "message.mobsmash_ext_species_fix.soul_inhibitor.output_on"
                        : "message.mobsmash_ext_species_fix.soul_inhibitor.output_off"), true);
        return InteractionResult.CONSUME;
    }

    @Override
    @SuppressWarnings("null")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    @SuppressWarnings("null")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide()) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SoulInhibitorBlockEntity tile)) return;
        ServerLevel sl = (ServerLevel) level;

        if (tile.getRedstoneMode() != SoulInhibitorRedstoneMode.PULSE) {
            applyModeAndRedstone(sl, pos);
            return;
        }

        if (!Config.soulInhibitorEnabled) return;

        boolean poweredNow = level.hasNeighborSignal(pos);
        boolean poweredBefore = state.getValue(POWERED);

        if (poweredNow && !poweredBefore) {
            if (Config.soulInhibitorEnabled) {
                level.setBlock(pos, state.setValue(POWERED, true).cycle(ON), Block.UPDATE_ALL);
            } else {
                level.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_ALL);
            }
            SoulInhibitorChunkIndex.refresh(sl, pos);
            return;
        }

        if (!poweredNow && poweredBefore) {
            level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
            SoulInhibitorChunkIndex.refresh(sl, pos);
        }
    }

    /**
     * Syncs block output from redstone according to {@link SoulInhibitorRedstoneMode}; refreshes chunk spawn map.
     */
    public static void applyModeAndRedstone(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SoulInhibitorBlock)) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SoulInhibitorBlockEntity tile)) {
            SoulInhibitorChunkIndex.refresh(level, pos);
            return;
        }

        if (!Config.soulInhibitorEnabled) {
            SoulInhibitorChunkIndex.refresh(level, pos);
            return;
        }

        boolean p = level.hasNeighborSignal(pos);
        switch (tile.getRedstoneMode()) {
            case PULSE -> SoulInhibitorChunkIndex.refresh(level, pos);
            case MANUAL -> {
                if (state.getValue(POWERED) != p) {
                    level.setBlock(pos, state.setValue(POWERED, p), Block.UPDATE_ALL);
                }
                SoulInhibitorChunkIndex.refresh(level, pos);
            }
            case HIGH -> {
                if (state.getValue(ON) != p || state.getValue(POWERED) != p) {
                    level.setBlock(pos, state.setValue(ON, p).setValue(POWERED, p), Block.UPDATE_ALL);
                }
                SoulInhibitorChunkIndex.refresh(level, pos);
            }
            case LOW -> {
                boolean on = !p;
                if (state.getValue(ON) != on || state.getValue(POWERED) != p) {
                    level.setBlock(pos, state.setValue(ON, on).setValue(POWERED, p), Block.UPDATE_ALL);
                }
                SoulInhibitorChunkIndex.refresh(level, pos);
            }
        }
    }

    @Override
    @SuppressWarnings("null")
    public boolean isSignalSource(BlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("null")
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.core.Direction direction) {
        return 0;
    }

    @Override
    @SuppressWarnings("null")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // no-op
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            if (state.getBlock() instanceof SoulInhibitorBlock) {
                SoulInhibitorChunkIndex.remove(sl, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoulInhibitorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }
}
