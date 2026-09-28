package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.BreathFeedback;

import java.time.Duration;
import java.util.Objects;

/**
 * Converte uma amostra [0,1] em um intervalo dentro da faixa pedida pelo profile.
 */
public final class ThermalFeedbackCadencePolicy {
    public Duration nextDelay(BreathFeedback breath, double randomUnit) {
        Objects.requireNonNull(breath, "feedback de respiração");
        if (!breath.enabled()) {
            return Duration.ZERO;
        }
        if (!Double.isFinite(randomUnit)
                || randomUnit < 0.0D
                || randomUnit > 1.0D) {
            throw new IllegalArgumentException(
                    "A amostra de jitter deve ficar entre 0 e 1"
            );
        }

        long minimumNanos = breath.minimumInterval().toNanos();
        long maximumNanos = breath.maximumInterval().toNanos();
        long rangeNanos = maximumNanos - minimumNanos;
        long jitterNanos = Math.round(rangeNanos * randomUnit);
        return Duration.ofNanos(minimumNanos + jitterNanos);
    }
}
