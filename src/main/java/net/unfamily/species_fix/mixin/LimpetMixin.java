package net.unfamily.species_fix.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.unfamily.species_fix.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Soft-scare: keep Species shell freeze vs players/entities (isPushable / canBeCollidedWith),
 * but do not zero XZ in travel so MGU Fan / Dark Utils vector plates / conveyors can move limpets.
 * <p>
 * Class target uses remap=false (mod class string). Inject uses remap=true so {@code travel}
 * resolves to the SRG name ({@code m_7023_}) in production Species jars.
 */
@Mixin(targets = "com.ninni.species.server.entity.mob.update_1.Limpet", remap = false)
public abstract class LimpetMixin extends PathfinderMob {

    protected LimpetMixin(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Inject(
            method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = true
    )
    private void species_fix$allowExternalPushWhenScared(Vec3 travelVector, CallbackInfo ci) {
        if (!Config.limpetFarmSoftScare) {
            return;
        }
        ci.cancel();
        super.travel(travelVector);
    }
}
