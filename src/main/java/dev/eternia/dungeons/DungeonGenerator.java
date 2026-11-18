package dev.eternia.dungeons;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Builds a compact, seed-varied stone maze on safe terrain near the expedition party. */
public final class DungeonGenerator {
    private static final int SIZE = 21;
    private DungeonGenerator() { }

    public record Site(int x, int y, int z) { }
    private record Cell(int x, int z) { }

    public static Site findSite(ServerLevel level, int nearX, int nearZ, long seed) {
        RandomSource random = RandomSource.create(seed);
        for (int radius = 32; radius <= 128; radius += 16) {
            int offset = random.nextInt(16);
            for (int i = 0; i < 16; i++) {
                double angle = (i + offset) * (Math.PI * 2.0 / 16.0);
                int centerX = nearX + (int) Math.round(Math.cos(angle) * radius);
                int centerZ = nearZ + (int) Math.round(Math.sin(angle) * radius);
                Site site = flatSite(level, centerX, centerZ);
                if (site != null) return site;
            }
        }
        return null;
    }

    private static Site flatSite(ServerLevel level, int centerX, int centerZ) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int x = centerX - 10; x <= centerX + 10; x += 2) {
            for (int z = centerZ - 10; z <= centerZ + 10; z += 2) {
                int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                min = Math.min(min, surface);
                max = Math.max(max, surface);
            }
        }
        if (max - min > 3) return null;
        int floorY = max - 1;
        BlockPos centerGround = new BlockPos(centerX, floorY, centerZ);
        if (!level.getFluidState(centerGround).isEmpty()) return null;
        return new Site(centerX - 10, floorY, centerZ - 10);
    }

    public static void build(ServerLevel level, Site site, long seed) {
        boolean[][] open = generateMaze(seed);
        // The final odd cell is always part of the carved maze. Expand it into
        // a clear arena so the boss never spawns inside a generated wall.
        for (int x = 17; x <= 19; x++) {
            for (int z = 17; z <= 19; z++) open[x][z] = true;
        }
        RandomSource random = RandomSource.create(seed ^ 0x4E5445524E49414CL);
        int ox = site.x();
        int oy = site.y();
        int oz = site.z();

        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int wx = ox + x;
                int wz = oz + z;
                int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, wx, wz) - 1;
                for (int fy = groundY + 1; fy < oy; fy++) {
                    level.setBlock(new BlockPos(wx, fy, wz), Blocks.STONE_BRICKS.defaultBlockState(), 3);
                }
                for (int y = oy + 1; y <= oy + 6; y++) {
                    level.setBlock(new BlockPos(wx, y, wz), Blocks.AIR.defaultBlockState(), 3);
                }
                level.setBlock(new BlockPos(wx, oy, wz), floorBlock(random).defaultBlockState(), 3);
                if (!open[x][z]) {
                    BlockState wall = wallBlock(random).defaultBlockState();
                    for (int h = 1; h <= 4; h++) level.setBlock(new BlockPos(wx, oy + h, wz), wall, 3);
                } else if (x % 4 == 1 && z % 4 == 1 && random.nextInt(3) == 0) {
                    level.setBlock(new BlockPos(wx, oy + 1, wz), Blocks.TORCH.defaultBlockState(), 3);
                }
                // A few missing roof stones let cold daylight spill into the ruin.
                if ((x * 13 + z * 7 + seed) % 17 != 0) {
                    level.setBlock(new BlockPos(wx, oy + 5, wz), Blocks.STONE_BRICKS.defaultBlockState(), 3);
                }
            }
        }

        // Mark the entrance with an old carved-stone arch and the far chamber with a blackstone dais.
        level.setBlock(new BlockPos(ox + 1, oy + 4, oz), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 3);
        for (int x = 17; x <= 19; x++) {
            for (int z = 17; z <= 19; z++) {
                level.setBlock(new BlockPos(ox + x, oy, oz + z), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3);
            }
        }
        for (int x : new int[]{0, 20}) {
            for (int z : new int[]{0, 20}) {
                for (int h = 1; h <= 5; h++) {
                    level.setBlock(new BlockPos(ox + x, oy + h, oz + z), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 3);
                }
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
            if (choices.isEmpty()) {
                stack.pop();
                continue;
            }
            Collections.shuffle(choices, new java.util.Random(random.nextLong()));
            int[] direction = choices.getFirst();
            int nx = current.x() + direction[0];
            int nz = current.z() + direction[1];
            open[current.x() + direction[0] / 2][current.z() + direction[1] / 2] = true;
            open[nx][nz] = true;
            stack.push(new Cell(nx, nz));
        }
        open[1][0] = true; // Entrance throat.
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
