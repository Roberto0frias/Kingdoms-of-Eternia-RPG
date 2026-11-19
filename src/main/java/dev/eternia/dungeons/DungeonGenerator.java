package dev.eternia.dungeons;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Large shared stone labyrinth used by the natural worldgen structure. */
public final class DungeonGenerator {
    public static final int SIZE = 41;
    public static final int BOSS_X = 36;
    public static final int BOSS_Z = 36;
    private DungeonGenerator() { }

    public record Site(int x, int y, int z) { }
    private record Cell(int x, int z) { }

    public static Site siteForChunk(WorldGenLevel level, BlockPos origin) {
        int ox = origin.getX() - SIZE / 2;
        int oz = origin.getZ() - SIZE / 2;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int x = ox; x <= ox + SIZE - 1; x += 4) {
            for (int z = oz; z <= oz + SIZE - 1; z += 4) {
                int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                min = Math.min(min, surface);
                max = Math.max(max, surface);
            }
        }
        if (max - min > 7) return null;
        int floorY = max - 1;
        BlockPos center = new BlockPos(origin.getX(), floorY, origin.getZ());
        if (!level.getFluidState(center).isEmpty()) return null;
        return new Site(ox, floorY, oz);
    }

    public static void build(LevelAccessor level, Site site, long seed) {
        boolean[][] open = generateMaze(seed);
        // Widen the connected final cell into a full arena for the shared boss encounter.
        for (int x = 33; x <= 39; x++) {
            for (int z = 33; z <= 39; z++) open[x][z] = true;
        }
        RandomSource random = RandomSource.create(seed ^ 0x4E5445524E49414CL);
        int ox = site.x();
        int oy = site.y();
        int oz = site.z();

        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int wx = ox + x;
                int wz = oz + z;
                int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, wx, wz) - 1;
                for (int fy = groundY + 1; fy < oy; fy++) {
                    level.setBlock(new BlockPos(wx, fy, wz), Blocks.STONE_BRICKS.defaultBlockState(), 3);
                }
                for (int y = oy + 1; y <= oy + 8; y++) {
                    level.setBlock(new BlockPos(wx, y, wz), Blocks.AIR.defaultBlockState(), 3);
                }
                level.setBlock(new BlockPos(wx, oy, wz), floorBlock(random).defaultBlockState(), 3);
                if (!open[x][z]) {
                    BlockState wall = wallBlock(random).defaultBlockState();
                    for (int h = 1; h <= 5; h++) level.setBlock(new BlockPos(wx, oy + h, wz), wall, 3);
                } else if (x % 6 == 1 && z % 6 == 1 && random.nextInt(2) == 0
                        && !(x >= 32 && z >= 32)) {
                    level.setBlock(new BlockPos(wx, oy + 1, wz), Blocks.SOUL_TORCH.defaultBlockState(), 3);
                }
                if ((x * 13 + z * 7 + seed) % 23 != 0) {
                    level.setBlock(new BlockPos(wx, oy + 7, wz), Blocks.STONE_BRICKS.defaultBlockState(), 3);
                }
            }
        }

        // Carved entrance arch marks a shared world structure that can be found while exploring.
        for (int x : new int[]{0, 2}) {
            for (int h = 1; h <= 4; h++) level.setBlock(new BlockPos(ox + x, oy + h, oz),
                    Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 3);
        }
        level.setBlock(new BlockPos(ox + 1, oy + 4, oz), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 3);
        level.setBlock(new BlockPos(ox + 1, oy, oz), Blocks.LODESTONE.defaultBlockState(), 3);

        // Nine-block dais, seven-by-seven arena and four tall marker pillars.
        for (int x = 33; x <= 39; x++) {
            for (int z = 33; z <= 39; z++) {
                level.setBlock(new BlockPos(ox + x, oy, oz + z), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3);
            }
        }
        for (int x = BOSS_X - 1; x <= BOSS_X + 1; x++) {
            for (int z = BOSS_Z - 1; z <= BOSS_Z + 1; z++) {
                level.setBlock(new BlockPos(ox + x, oy, oz + z), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 3);
            }
        }
        for (int x : new int[]{33, 39}) {
            for (int z : new int[]{33, 39}) {
                for (int h = 1; h <= 6; h++) level.setBlock(new BlockPos(ox + x, oy + h, oz + z),
                        Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3);
                level.setBlock(new BlockPos(ox + x, oy + 7, oz + z), Blocks.SOUL_LANTERN.defaultBlockState(), 3);
            }
        }
    }

    private static boolean[][] generateMaze(long seed) {
        boolean[][] open = new boolean[SIZE][SIZE];
        RandomSource random = RandomSource.create(seed);
        Deque<Cell> stack = new ArrayDeque<>();
        open[1][1] = true;
        stack.push(new Cell(1, 1));
        int[][] directions = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
        while (!stack.isEmpty()) {
            Cell current = stack.peek();
            List<int[]> choices = new ArrayList<>();
            for (int[] direction : directions) {
                int nx = current.x() + direction[0];
                int nz = current.z() + direction[1];
                if (nx > 0 && nx < SIZE - 1 && nz > 0 && nz < SIZE - 1 && !open[nx][nz]) choices.add(direction);
            }
            if (choices.isEmpty()) { stack.pop(); continue; }
            Collections.shuffle(choices, new java.util.Random(random.nextLong()));
            int[] direction = choices.getFirst();
            int nx = current.x() + direction[0];
            int nz = current.z() + direction[1];
            open[current.x() + direction[0] / 2][current.z() + direction[1] / 2] = true;
            open[nx][nz] = true;
            stack.push(new Cell(nx, nz));
        }
        open[1][0] = true;
        return open;
    }

    private static net.minecraft.world.level.block.Block floorBlock(RandomSource random) {
        return switch (random.nextInt(8)) {
            case 0 -> Blocks.CRACKED_STONE_BRICKS;
            case 1 -> Blocks.MOSSY_STONE_BRICKS;
            case 2 -> Blocks.COBBLESTONE;
            default -> Blocks.STONE_BRICKS;
        };
    }

    private static net.minecraft.world.level.block.Block wallBlock(RandomSource random) {
        return switch (random.nextInt(9)) {
            case 0 -> Blocks.CRACKED_STONE_BRICKS;
            case 1 -> Blocks.MOSSY_STONE_BRICKS;
            case 2 -> Blocks.CHISELED_STONE_BRICKS;
            default -> Blocks.STONE_BRICKS;
        };
    }
}
