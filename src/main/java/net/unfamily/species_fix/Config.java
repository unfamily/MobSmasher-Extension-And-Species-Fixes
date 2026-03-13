package net.unfamily.species_fix;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = SpeciesFixes.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue SCREEN_SHAKE_FIX = BUILDER
            .comment("When true, prevents crash when camera is null during Species screen shake (recommended: true).")
            .define("000_screenShakeFix", true);

    private static final ForgeConfigSpec.BooleanValue QUAKE_SAW_KILL = BUILDER
            .comment("When true, Quake is killed instantly when hit by Mob Grinding Utils Saw (FakePlayer), no immunity/parry.")
            .define("002_quakeSawKill", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    /** Read by ScreenShakeEventMixin; true = apply null-check and return 0 when camera is null. */
    public static boolean screenShakeFixEnabled = true;
    /** Read by SawQuakeKillHandler + mixins; true = Saw/FakePlayer kills Quake instantly. */
    public static boolean quakeSawKillEnabled = true;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        screenShakeFixEnabled = SCREEN_SHAKE_FIX.get();
        quakeSawKillEnabled = QUAKE_SAW_KILL.get();
    }
}
