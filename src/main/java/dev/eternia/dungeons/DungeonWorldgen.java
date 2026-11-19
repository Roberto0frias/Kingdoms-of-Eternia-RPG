package dev.eternia.dungeons;

import dev.eternia.core.EterniaMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class DungeonWorldgen {
    public static final ThresholdRuinFeature THRESHOLD_RUIN = Registry.register(
            BuiltInRegistries.FEATURE, EterniaMod.id("threshold_ruin"), new ThresholdRuinFeature());
    private static final ResourceKey<PlacedFeature> PLACED_RUIN = ResourceKey.create(
            Registries.PLACED_FEATURE, EterniaMod.id("threshold_ruin"));

    private DungeonWorldgen() { }

    public static void register() {
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.SURFACE_STRUCTURES, PLACED_RUIN);
    }
}
