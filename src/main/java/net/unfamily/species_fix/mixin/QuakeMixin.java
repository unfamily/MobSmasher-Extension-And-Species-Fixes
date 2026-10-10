package net.unfamily.species_fix.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Quake immunity is based on: getAttackCooldown() (when != 0 it "shields") and pose ATTACK/RECHARGE (absorbs).
 * Saw (MGUFakePlayer): when config enabled, instant kill, no shield/absorb.
 * String target keeps this mixin loadable when Species is absent (plugin skips apply).
 */
@Mixin(targets = "com.ninni.species.server.entity.mob.update_3.Quake", remap = false)
public class QuakeMixin {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("SpeciesFix/QuakeMixin");

    static {
        LOGGER.info("[Species Fix] QuakeMixin loaded (Saw instant kill active)");
    }

    private static boolean isMobSmasherDamage(DamageSource source) {
        if (source.getEntity() == null) return false;
        String name = source.getEntity().getClass().getName();
        return name.contains("MGUFakePlayer") || name.contains("FakePlayer") || name.contains("mob_grinding_utils");
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true, remap = true)
    private void species_fix$bypassImmunityOrDieIfLethal(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        var id = BuiltInRegistries.ENTITY_TYPE.getKey(self.getType());
        if (id != null
                && net.unfamily.species_fix.Config.sawKillEntityIds.contains(id)
                && isMobSmasherDamage(damageSource)) {
            self.setHealth(0);
            self.die(damageSource);
            LOGGER.info("[Species Fix] Saw damage killed Quake (QuakeMixin.hurt)");
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
