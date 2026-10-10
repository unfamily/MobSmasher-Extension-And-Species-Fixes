package net.unfamily.species_fix.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents NPE when cameraEntity is null (e.g. during loading or in some modpack setups).
 * Species calls getDegree(cameraEntity, partialTicks) from ClientEvents.clientTick without
 * null-checking mc.getCameraEntity().
 * String target keeps this mixin loadable when Species is absent (plugin skips apply).
 */
@Mixin(targets = "com.ninni.species.client.screen.ScreenShakeEvent", remap = false)
public class ScreenShakeEventMixin {

    static {
        org.slf4j.LoggerFactory.getLogger("SpeciesFix/Shake").info("[Species Fix] ScreenShakeEventMixin loaded (screen shake NPE fix active)");
    }

    @Inject(method = "getDegree(Lnet/minecraft/world/entity/Entity;F)F", at = @At("HEAD"), cancellable = true)
    private void species_fix$nullCheckCamera(Entity cameraEntity, float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (cameraEntity == null && net.unfamily.species_fix.Config.screenShakeFixEnabled) {
            cir.setReturnValue(0.0F);
        }
    }
}
