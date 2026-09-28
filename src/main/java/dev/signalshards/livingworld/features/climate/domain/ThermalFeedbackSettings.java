package dev.signalshards.livingworld.features.climate.domain;

import java.time.Duration;
import java.util.Objects;

/**
 * Tuning inicial do feedback térmico Vanilla+.
 */
public record ThermalFeedbackSettings(
        double breathStartCelsius,
        double breathFullCelsius,
        Duration mildBreathMinimumInterval,
        Duration mildBreathMaximumInterval,
        Duration severeBreathMinimumInterval,
        Duration severeBreathMaximumInterval,
        double frostStartLoad,
        double frostFullLoad
) {
    public ThermalFeedbackSettings {
        if (!Double.isFinite(breathStartCelsius)
                || !Double.isFinite(breathFullCelsius)
                || breathFullCelsius >= breathStartCelsius) {
            throw new IllegalArgumentException(
                    "Breath full deve ser mais frio que breath start"
            );
        }
        validateRange(
                mildBreathMinimumInterval,
                mildBreathMaximumInterval,
                "respiração leve"
        );
        validateRange(
                severeBreathMinimumInterval,
                severeBreathMaximumInterval,
                "respiração severa"
        );
        if (severeBreathMinimumInterval.compareTo(mildBreathMinimumInterval) > 0
                || severeBreathMaximumInterval.compareTo(mildBreathMaximumInterval) > 0) {
            throw new IllegalArgumentException(
                    "Frio severo deve respirar em cadência igual ou mais rápida"
            );
        }
        if (!Double.isFinite(frostStartLoad)
                || !Double.isFinite(frostFullLoad)
                || frostFullLoad >= frostStartLoad
                || frostStartLoad > 0.0D
                || frostFullLoad < PlayerThermalState.MIN_LOAD) {
            throw new IllegalArgumentException(
                    "A faixa de frost deve ser negativa e crescente em severidade"
            );
        }
    }

    public static ThermalFeedbackSettings livingWorldDefaults() {
        return new ThermalFeedbackSettings(
                8.0D,
                -10.0D,
                Duration.ofMillis(3_500),
                Duration.ofMillis(5_500),
                Duration.ofMillis(1_200),
                Duration.ofMillis(2_500),
                -0.30D,
                -0.85D
        );
    }

    private static void validateRange(
            Duration minimum,
            Duration maximum,
            String name
    ) {
        Objects.requireNonNull(minimum, "mínimo de " + name);
        Objects.requireNonNull(maximum, "máximo de " + name);
        if (minimum.isZero()
                || minimum.isNegative()
                || maximum.isZero()
                || maximum.isNegative()
                || minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException(
                    "Intervalos inválidos para " + name
            );
        }
    }
}
