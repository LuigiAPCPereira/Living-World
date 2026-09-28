package dev.signalshards.livingworld.features.climate.domain;

import java.time.Duration;
import java.util.Objects;

/**
 * Pedido abstrato de respiração visível, sem escolher partícula/packet.
 */
public record BreathFeedback(
        double intensity,
        Duration minimumInterval,
        Duration maximumInterval
) {
    public BreathFeedback {
        if (!Double.isFinite(intensity) || intensity < 0.0D || intensity > 1.0D) {
            throw new IllegalArgumentException(
                    "A intensidade da respiração deve ficar entre 0 e 1"
            );
        }
        Objects.requireNonNull(minimumInterval, "intervalo mínimo");
        Objects.requireNonNull(maximumInterval, "intervalo máximo");
        if (minimumInterval.isNegative() || maximumInterval.isNegative()) {
            throw new IllegalArgumentException(
                    "Intervalos de respiração não podem ser negativos"
            );
        }
        if (minimumInterval.compareTo(maximumInterval) > 0) {
            throw new IllegalArgumentException(
                    "O intervalo mínimo não pode exceder o máximo"
            );
        }
        if (intensity == 0.0D
                && (!minimumInterval.isZero() || !maximumInterval.isZero())) {
            throw new IllegalArgumentException(
                    "Respiração desabilitada deve usar intervalos zero"
            );
        }
        if (intensity > 0.0D && minimumInterval.isZero()) {
            throw new IllegalArgumentException(
                    "Respiração habilitada exige intervalo mínimo positivo"
            );
        }
    }

    public static BreathFeedback none() {
        return new BreathFeedback(
                0.0D,
                Duration.ZERO,
                Duration.ZERO
        );
    }

    public boolean enabled() {
        return intensity > 0.0D;
    }
}
