package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Fallback vanilla self-only para sensação visual de frio corporal.
 *
 * <p>Não usa freeze ticks nem transmite frost para outros jogadores.</p>
 */
public final class PaperSnowflakeFrostPresenter
        implements PaperFrostFeedbackPresenter {
    @Override
    public void presentFrost(Player player, double intensity) {
        Objects.requireNonNull(player, "jogador");
        if (!Double.isFinite(intensity) || intensity <= 0.0D || intensity > 1.0D) {
            throw new IllegalArgumentException(
                    "A intensidade de frost deve ficar em (0,1]"
            );
        }
        int count = particleCount(intensity);
        double horizontalSpread = 0.16D + (0.18D * intensity);
        double verticalSpread = 0.10D + (0.10D * intensity);
        player.spawnParticle(
                Particle.SNOWFLAKE,
                player.getEyeLocation(),
                count,
                horizontalSpread,
                verticalSpread,
                horizontalSpread,
                0.002D + (0.004D * intensity)
        );
    }

    static int particleCount(double intensity) {
        if (!Double.isFinite(intensity) || intensity <= 0.0D || intensity > 1.0D) {
            throw new IllegalArgumentException(
                    "A intensidade deve ficar em (0,1]"
            );
        }
        if (intensity < 0.40D) {
            return 1;
        }
        return intensity < 0.75D ? 2 : 3;
    }
}
