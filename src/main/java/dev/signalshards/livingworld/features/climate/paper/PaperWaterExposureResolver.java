package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.Objects;
import java.util.function.IntPredicate;

/**
 * Resolve contato/submersão/profundidade usando somente a coluna local do jogador.
 */
public final class PaperWaterExposureResolver {
    private static final double[] BODY_SAMPLE_FRACTIONS = {0.10D, 0.35D, 0.65D, 0.90D};
    static final int MAX_DEPTH_PROBE_BLOCKS = 32;

    private PaperWaterExposureResolver() {
    }

    public static WaterExposure forPlayer(Player player) {
        Objects.requireNonNull(player, "jogador");
        World world = player.getWorld();
        BoundingBox bounds = player.getBoundingBox();
        int x = floor((bounds.getMinX() + bounds.getMaxX()) * 0.5D);
        int z = floor((bounds.getMinZ() + bounds.getMaxZ()) * 0.5D);
        double height = Math.max(0.01D, bounds.getHeight());

        int wetSamples = 0;
        for (double bodyFraction : BODY_SAMPLE_FRACTIONS) {
            int y = floor(bounds.getMinY() + (height * bodyFraction));
            if (isWater(world, x, y, z)) {
                wetSamples++;
            }
        }
        double submergedFraction = wetSamples / (double) BODY_SAMPLE_FRACTIONS.length;
        if (submergedFraction == 0.0D) {
            return WaterExposure.none();
        }
        if (submergedFraction < 1.0D) {
            return new WaterExposure(submergedFraction, 0.0D);
        }

        int referenceY = floor(bounds.getMaxY() - 0.01D);
        double depth = depthBelowSurface(
                offset -> isWater(world, x, referenceY + offset, z),
                MAX_DEPTH_PROBE_BLOCKS
        );
        return new WaterExposure(1.0D, depth);
    }

    static double submergedFraction(boolean... waterSamples) {
        Objects.requireNonNull(waterSamples, "amostras de água");
        if (waterSamples.length == 0) {
            throw new IllegalArgumentException("É necessária ao menos uma amostra corporal");
        }
        int wet = 0;
        for (boolean water : waterSamples) {
            if (water) {
                wet++;
            }
        }
        return wet / (double) waterSamples.length;
    }

    static int depthBelowSurface(IntPredicate waterAtOffset, int maxDepth) {
        Objects.requireNonNull(waterAtOffset, "sonda de água");
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("A profundidade máxima deve ser positiva");
        }
        if (!waterAtOffset.test(0)) {
            return 0;
        }

        int lastWater = 0;
        int probe = 1;
        while (probe <= maxDepth && waterAtOffset.test(probe)) {
            lastWater = probe;
            if (probe == maxDepth) {
                return maxDepth;
            }
            probe = Math.min(maxDepth, probe * 2);
        }
        if (probe == maxDepth && waterAtOffset.test(probe)) {
            return maxDepth;
        }

        int low = lastWater + 1;
        int high = probe;
        while (low < high) {
            int middle = low + ((high - low) / 2);
            if (waterAtOffset.test(middle)) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return Math.max(0, low - 1);
    }

    static boolean isWaterMaterial(Material material, boolean waterlogged) {
        return material == Material.WATER
                || material == Material.BUBBLE_COLUMN
                || waterlogged;
    }

    private static boolean isWater(World world, int x, int y, int z) {
        if (y < world.getMinHeight() || y >= world.getMaxHeight()) {
            return false;
        }
        Block block = world.getBlockAt(x, y, z);
        boolean waterlogged = block.getBlockData() instanceof Waterlogged data
                && data.isWaterlogged();
        return isWaterMaterial(block.getType(), waterlogged);
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }
}
