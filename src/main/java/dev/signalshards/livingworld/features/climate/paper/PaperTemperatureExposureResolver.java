package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.TemperatureExposure;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class PaperTemperatureExposureResolver {
    private PaperTemperatureExposureResolver() {
    }

    public static TemperatureExposure forPlayer(Player player) {
        Objects.requireNonNull(player, "jogador");
        if (player.isInLava()) {
            return TemperatureExposure.LAVA;
        }
        if (player.isInWater()) {
            return TemperatureExposure.WATER;
        }
        if (player.getFireTicks() > 0) {
            return TemperatureExposure.FIRE;
        }
        return TemperatureExposure.NONE;
    }
}
