package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.DirectThermalExposure;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Resolve contato térmico físico direto usando apenas flags do próprio Player.
 */
public final class PaperDirectThermalExposureResolver {
    private PaperDirectThermalExposureResolver() {
    }

    public static DirectThermalExposure forPlayer(Player player) {
        Objects.requireNonNull(player, "jogador");
        return resolve(
                player.isInLava(),
                player.isInPowderedSnow(),
                player.isInWater(),
                player.getFireTicks()
        );
    }

    static DirectThermalExposure resolve(
            boolean inLava,
            boolean inPowderSnow,
            boolean inWater,
            int fireTicks
    ) {
        if (inLava) {
            return DirectThermalExposure.LAVA;
        }
        if (inPowderSnow) {
            return DirectThermalExposure.POWDER_SNOW;
        }
        if (inWater) {
            return DirectThermalExposure.NONE;
        }
        if (fireTicks > 0) {
            return DirectThermalExposure.FIRE;
        }
        return DirectThermalExposure.NONE;
    }
}
