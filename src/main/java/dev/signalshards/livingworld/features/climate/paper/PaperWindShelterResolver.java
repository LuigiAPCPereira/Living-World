package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * Resolve vento/abrigo a partir de observações locais O(1), sem detectar casas.
 */
public final class PaperWindShelterResolver {
    private static final double ALTITUDE_FULL_EXPOSURE_BLOCKS = 96.0D;
    private static final double MOVEMENT_FULL_EXPOSURE_BLOCKS_PER_TICK = 1.20D;

    private PaperWindShelterResolver() {
    }

    public static Observation forPlayer(Player player) {
        Objects.requireNonNull(player, "jogador");
        World world = player.getWorld();
        double skyExposure = Math.clamp(
                player.getEyeLocation().getBlock().getLightFromSky() / 15.0D,
                0.0D,
                1.0D
        );
        double weatherStrength = world.isThundering()
                ? 1.0D
                : world.hasStorm() ? 0.70D : 0.0D;
        double altitudeStrength = Math.clamp(
                (player.getLocation().getY() - world.getSeaLevel())
                        / ALTITUDE_FULL_EXPOSURE_BLOCKS,
                0.0D,
                1.0D
        );
        Vector velocity = player.getVelocity();
        double horizontalSpeed = Math.hypot(velocity.getX(), velocity.getZ());
        double movementStrength = Math.clamp(
                horizontalSpeed / MOVEMENT_FULL_EXPOSURE_BLOCKS_PER_TICK,
                0.0D,
                1.0D
        );
        if (player.isGliding()) {
            movementStrength = Math.max(movementStrength, 0.75D);
        }
        return resolve(
                skyExposure,
                weatherStrength,
                altitudeStrength,
                movementStrength
        );
    }

    static Observation resolve(
            double skyExposure,
            double weatherStrength,
            double altitudeStrength,
            double movementStrength
    ) {
        validateUnit(skyExposure, "exposição ao céu");
        validateUnit(weatherStrength, "força do weather");
        validateUnit(altitudeStrength, "exposição de altitude");
        validateUnit(movementStrength, "exposição por movimento");

        double environmentalWind = Math.clamp(
                0.10D + (weatherStrength * 0.55D) + (altitudeStrength * 0.35D),
                0.0D,
                1.0D
        );
        double wind = skyExposure * Math.max(environmentalWind, movementStrength);
        double shelter = 1.0D - skyExposure;
        return new Observation(
                new WindExposure(Math.clamp(wind, 0.0D, 1.0D)),
                new ShelterFactor(Math.clamp(shelter, 0.0D, 1.0D))
        );
    }

    private static void validateUnit(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D || value > 1.0D) {
            throw new IllegalArgumentException(name + " deve ficar entre 0 e 1");
        }
    }

    public record Observation(
            WindExposure windExposure,
            ShelterFactor shelterFactor
    ) {
        public Observation {
            Objects.requireNonNull(windExposure, "vento");
            Objects.requireNonNull(shelterFactor, "abrigo");
        }
    }
}
