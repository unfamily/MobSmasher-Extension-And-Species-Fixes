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

    private static final ForgeConfigSpec.BooleanValue SOUL_INHIBITOR_ENABLED = BUILDER
            .comment("When true: Soul Inhibitor ON state registers chunk coverage; use/redstone toggle apply; nearby Forbidden Arcanus lost_soul joins are canceled.")
            .define("010_soulInhibitorEnabled", true);

    private static final ForgeConfigSpec.IntValue SOUL_INHIBITOR_RADIUS = BUILDER
            .comment("Block radius (axis-aligned cube) for chunk registration; all chunks intersecting the cube get coverage. Not a sphere.")
            .defineInRange("011_soulInhibitorRadius", 16, 1, 128);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    /** Read by ScreenShakeEventMixin; true = apply null-check and return 0 when camera is null. */
    public static boolean screenShakeFixEnabled = true;
    /** Read by SawQuakeKillHandler + mixins; set of entity IDs (modid:entity) killed instantly by Saw/FakePlayer. */
    public static java.util.Set<ResourceLocation> sawKillEntityIds = java.util.Set.of();
    /** Soul Inhibitor: interaction + spawn filter when true. */
    public static boolean soulInhibitorEnabled = true;
    /** Half-size of the axis-aligned cube (in blocks) for chunk coverage around each active inhibitor. */
    public static int soulInhibitorRadius = 16;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        screenShakeFixEnabled = SCREEN_SHAKE_FIX.get();
        soulInhibitorEnabled = SOUL_INHIBITOR_ENABLED.get();
        soulInhibitorRadius = SOUL_INHIBITOR_RADIUS.get();
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

        if (event instanceof ModConfigEvent.Reloading) {
            var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                SoulInhibitorChunkIndex.refreshAll(server);
            }
        }
    }
}
