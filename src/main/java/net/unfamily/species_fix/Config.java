package net.unfamily.species_fix;

import net.minecraft.resources.ResourceLocation;
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

    private static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> SAW_KILL_ENTITIES = BUILDER
            .comment("List of entity ids (modid:entity) that should be killed instantly when hit by Mob Grinding Utils Saw (FakePlayer).")
            .defineList("001_sawKillEntities",
                    java.util.List.of(
                            "species:quake",
                            "species:limpet",
                            "mowziesmobs:grottol"
                    ),
                    o -> o instanceof String);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    /** Read by ScreenShakeEventMixin; true = apply null-check and return 0 when camera is null. */
    public static boolean screenShakeFixEnabled = true;
    /** Read by SawQuakeKillHandler + mixins; set of entity IDs (modid:entity) killed instantly by Saw/FakePlayer. */
    public static java.util.Set<ResourceLocation> sawKillEntityIds = java.util.Set.of();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        screenShakeFixEnabled = SCREEN_SHAKE_FIX.get();
        java.util.List<? extends String> raw = SAW_KILL_ENTITIES.get();
        java.util.Set<ResourceLocation> parsed = new java.util.HashSet<>();
        for (String s : raw) {
            if (s == null) continue;
            ResourceLocation id = ResourceLocation.tryParse(s);
            if (id != null) {
                parsed.add(id);
            }
        }
        sawKillEntityIds = java.util.Collections.unmodifiableSet(parsed);
    }
}
