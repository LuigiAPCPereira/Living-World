package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.LocalHeatExposure;
import dev.signalshards.livingworld.features.climate.domain.LocalHeatSourceType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Descobre fontes de calor por raios fixos bounded, sem scan volumétrico.
 */
public final class PaperLocalHeatSourceResolver {
    static final int MAX_PROBES = 96;
    static final int MAX_SOURCES = 8;
    private static final int PRIMARY_RAY_LENGTH = 3;
    private static final int AXIS_RAY_LENGTH = 6;
    private static final int[][] DIRECTIONS = directions();

    private PaperLocalHeatSourceResolver() {
    }

    public static List<LocalHeatExposure> forPlayer(Player player) {
        Objects.requireNonNull(player, "jogador");
        World world = player.getWorld();
        var eye = player.getEyeLocation();
        int originX = eye.getBlockX();
        int originY = eye.getBlockY();
        int originZ = eye.getBlockZ();
        List<LocalHeatExposure> sources = new ArrayList<>(MAX_SOURCES);
        Set<Long> visited = new HashSet<>(MAX_PROBES);
        int probes = 0;

        for (int[] direction : DIRECTIONS) {
            int rayLength = isAxis(direction) ? AXIS_RAY_LENGTH : PRIMARY_RAY_LENGTH;
            for (int step = 1; step <= rayLength && probes < MAX_PROBES; step++) {
                int x = originX + (direction[0] * step);
                int y = originY + (direction[1] * step);
                int z = originZ + (direction[2] * step);
                if (y < world.getMinHeight() || y >= world.getMaxHeight()) {
                    break;
                }
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    break;
                }
                long key = blockKey(x, y, z);
                if (!visited.add(key)) {
                    continue;
                }
                probes++;
                Block block = world.getBlockAt(x, y, z);
                Optional<LocalHeatSourceType> source = sourceType(
                        block.getType(),
                        isLit(block)
                );
                if (source.isPresent()) {
                    double distance = Math.sqrt(
                            (direction[0] * direction[0]
                                    + direction[1] * direction[1]
                                    + direction[2] * direction[2])
                                    * (double) step * step
                    );
                    sources.add(new LocalHeatExposure(
                            source.orElseThrow(),
                            distance,
                            1.0D,
                            1.0D
                    ));
                    if (sources.size() >= MAX_SOURCES) {
                        return List.copyOf(sources);
                    }
                }
                if (block.getType().isOccluding()) {
                    break;
                }
            }
            if (probes >= MAX_PROBES) {
                break;
            }
        }
        return List.copyOf(sources);
    }

    static Optional<LocalHeatSourceType> sourceType(Material material, boolean lit) {
        Objects.requireNonNull(material, "material");
        return switch (material) {
            case TORCH, WALL_TORCH, SOUL_TORCH, SOUL_WALL_TORCH ->
                    Optional.of(LocalHeatSourceType.TORCH);
            case LANTERN, SOUL_LANTERN -> Optional.of(LocalHeatSourceType.LANTERN);
            case FURNACE -> lit
                    ? Optional.of(LocalHeatSourceType.LIT_FURNACE)
                    : Optional.empty();
            case SMOKER -> lit
                    ? Optional.of(LocalHeatSourceType.LIT_SMOKER)
                    : Optional.empty();
            case BLAST_FURNACE -> lit
                    ? Optional.of(LocalHeatSourceType.LIT_BLAST_FURNACE)
                    : Optional.empty();
            case FIRE, SOUL_FIRE -> Optional.of(LocalHeatSourceType.FIRE);
            case CAMPFIRE, SOUL_CAMPFIRE -> lit
                    ? Optional.of(LocalHeatSourceType.CAMPFIRE)
                    : Optional.empty();
            case LAVA -> Optional.of(LocalHeatSourceType.LAVA);
            case MAGMA_BLOCK -> Optional.of(LocalHeatSourceType.MAGMA_BLOCK);
            default -> Optional.empty();
        };
    }

    private static boolean isLit(Block block) {
        return !(block.getBlockData() instanceof Lightable lightable)
                || lightable.isLit();
    }

    private static boolean isAxis(int[] direction) {
        int nonZero = 0;
        for (int component : direction) {
            if (component != 0) {
                nonZero++;
            }
        }
        return nonZero == 1;
    }

    private static int[][] directions() {
        List<int[]> directions = new ArrayList<>(26);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        directions.add(new int[]{x, y, z});
                    }
                }
            }
        }
        return directions.toArray(int[][]::new);
    }

    private static long blockKey(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38)
                | ((long) (z & 0x3FFFFFF) << 12)
                | (y & 0xFFFL);
    }
}
