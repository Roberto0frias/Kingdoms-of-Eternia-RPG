package dev.eternia.dungeons;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Places the Umbral Ruin as shared Overworld world generation, rather than a player instance. */
public final class ThresholdRuinFeature extends Feature<NoneFeatureConfiguration> {
    public ThresholdRuinFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        DungeonGenerator.Site site = DungeonGenerator.siteForChunk(level, context.origin());
        if (site == null) return false;

        long seed = context.random().nextLong();
        DungeonGenerator.build(level, site, seed);
        String dungeonId = java.util.UUID.nameUUIDFromBytes((level.getSeed() + ":threshold-ruin:"
                + site.x() + ":" + site.z()).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
        DungeonNpc.spawn(level, site.x() + 1.5, site.y() + 1.0, site.z() + 1.5, dungeonId);
        DungeonBosses.spawn(level, site.x() + DungeonGenerator.BOSS_X + 0.5, site.y() + 1.0,
                site.z() + DungeonGenerator.BOSS_Z + 0.5, dungeonId, null);
        return true;
    }
}
