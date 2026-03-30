package net.unfamily.species_fix;

import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

/**
 * Pylons-style: check precomputed chunk coverage, then cancel join for filtered mobs only.
 */
public class SoulInhibitorSpawnHandler {
    private static final String FA_ENTITY_DESC_PREFIX = "entity.forbidden_arcanus.";

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!ModList.get().isLoaded("forbidden_arcanus")) return;
        if (!Config.soulInhibitorEnabled) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) return;
        if (!isForbiddenArcanusLostSoul((LivingEntity) entity)) return;

        // Same chunk key as Pylons SpawnManager (entity position, not floored block pos)
        int cx = SectionPos.posToSectionCoord(entity.getX());
        int cz = SectionPos.posToSectionCoord(entity.getZ());
        if (SoulInhibitorChunkIndex.getChunkCoverageCount(level, cx, cz) <= 0) return;

        event.setCanceled(true);
    }

    private static boolean isForbiddenArcanusLostSoul(LivingEntity entity) {
        String desc = entity.getType().getDescriptionId();
        return desc.startsWith(FA_ENTITY_DESC_PREFIX) && desc.contains("lost_soul");
    }
}
