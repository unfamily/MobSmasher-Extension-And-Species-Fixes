package net.unfamily.species_fix.mixin;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Applies Species-targeted mixins only when the Species mod is present.
 * Keeps this mod loadable when Species is optional / absent.
 */
public class SpeciesFixesMixinPlugin implements IMixinConfigPlugin {

    private static final Set<String> SPECIES_MIXINS = Set.of(
            "net.unfamily.species_fix.mixin.QuakeMixin",
            "net.unfamily.species_fix.mixin.ScreenShakeEventMixin",
            "net.unfamily.species_fix.mixin.LimpetMixin"
    );

    private static final boolean SPECIES_LOADED = LoadingModList.get().getModFileById("species") != null;

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (SPECIES_MIXINS.contains(mixinClassName)) {
            return SPECIES_LOADED;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
