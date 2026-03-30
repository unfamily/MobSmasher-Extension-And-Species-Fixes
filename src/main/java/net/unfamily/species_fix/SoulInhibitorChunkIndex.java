package net.unfamily.species_fix;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.unfamily.species_fix.block.SoulInhibitorBlock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pylons-style chunk coverage: same deferred snapshot as {@code SpawnManager} — the map read from
 * {@link net.minecraftforge.event.entity.EntityJoinLevelEvent} is rebuilt only at {@link TickEvent.Phase#END},
 * so it is never mutated during entity join or block update paths (avoids re-entrancy / lock ordering issues).
 */
@Mod.EventBusSubscriber(modid = SpeciesFixes.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SoulInhibitorChunkIndex {
    private record ChunkKey(ResourceKey<Level> dimension, int chunkX, int chunkZ) {}
    private record InhibitorKey(ResourceKey<Level> dimension, BlockPos pos) {}

    /** Authoritative which chunks each active inhibitor covers; updated only on refresh/remove (server thread). */
    private static final Map<InhibitorKey, Set<ChunkKey>> INHIBITOR_TO_CHUNKS = new ConcurrentHashMap<>();

    /**
     * Read-only snapshot for spawn checks; swapped at server tick END when {@link #dirty} (same idea as Pylons {@code chunkMap}).
     */
    private static volatile Map<ChunkKey, Integer> chunkCountsSnapshot = Map.of();

    private static volatile boolean dirty = true;

    private SoulInhibitorChunkIndex() {}

    @SubscribeEvent
    public static void onServerTickEnd(TickEvent.ServerTickEvent event) {
        if (!event.side.isServer() || event.phase != TickEvent.Phase.END) return;
        if (!dirty) return;
        dirty = false;
        Map<ChunkKey, Integer> next = new HashMap<>();
        for (Set<ChunkKey> chunks : INHIBITOR_TO_CHUNKS.values()) {
            for (ChunkKey ck : chunks) {
                next.merge(ck, 1, Integer::sum);
            }
        }
        chunkCountsSnapshot = next.isEmpty() ? Map.of() : Map.copyOf(next);
    }

    public static void refresh(ServerLevel level, BlockPos pos) {
        InhibitorKey key = new InhibitorKey(level.dimension(), pos.immutable());
        INHIBITOR_TO_CHUNKS.remove(key);

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof SoulInhibitorBlock && Config.soulInhibitorEnabled && state.getValue(SoulInhibitorBlock.ON)) {
            int r = Math.max(1, Config.soulInhibitorRadius);
            Set<ChunkKey> chunks = chunksIntersectingCube(level.dimension(), pos, r);
            INHIBITOR_TO_CHUNKS.put(key, chunks);
        }
        dirty = true;
    }

    public static void remove(ServerLevel level, BlockPos pos) {
        InhibitorKey key = new InhibitorKey(level.dimension(), pos.immutable());
        if (INHIBITOR_TO_CHUNKS.remove(key) != null) {
            dirty = true;
        }
    }

    /** After config reload (e.g. radius): rebuild all known inhibitors. */
    public static void refreshAll(MinecraftServer server) {
        Set<InhibitorKey> keys = Set.copyOf(INHIBITOR_TO_CHUNKS.keySet());
        for (InhibitorKey key : keys) {
            ServerLevel sl = server.getLevel(key.dimension());
            if (sl == null) continue;
            refresh(sl, key.pos());
        }
    }

    public static int getChunkCoverageCount(ServerLevel level, int chunkX, int chunkZ) {
        ChunkKey ck = new ChunkKey(level.dimension(), chunkX, chunkZ);
        return chunkCountsSnapshot.getOrDefault(ck, 0);
    }

    private static Set<ChunkKey> chunksIntersectingCube(ResourceKey<Level> dim, BlockPos center, int radiusBlocks) {
        int minX = center.getX() - radiusBlocks;
        int maxX = center.getX() + radiusBlocks;
        int minZ = center.getZ() - radiusBlocks;
        int maxZ = center.getZ() + radiusBlocks;
        int minCx = SectionPos.blockToSectionCoord(minX);
        int maxCx = SectionPos.blockToSectionCoord(maxX);
        int minCz = SectionPos.blockToSectionCoord(minZ);
        int maxCz = SectionPos.blockToSectionCoord(maxZ);
        Set<ChunkKey> out = new HashSet<>();
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                out.add(new ChunkKey(dim, cx, cz));
            }
        }
        return out;
    }
}
