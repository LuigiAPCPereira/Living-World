package dev.signalshards.livingworld.features.climate.application;

import java.time.Duration;
import java.util.Objects;

/**
 * Limites de integração temporal para manter custo previsível.
 */
public record ThermalSimulationSettings(
        Duration maximumStep,
        Duration maximumCatchUp
) {
    public ThermalSimulationSettings {
        Objects.requireNonNull(maximumStep, "passo máximo");
        Objects.requireNonNull(maximumCatchUp, "catch-up máximo");
        if (maximumStep.isZero() || maximumStep.isNegative()) {
            throw new IllegalArgumentException("O passo máximo deve ser positivo");
        }
        if (maximumCatchUp.isZero() || maximumCatchUp.isNegative()) {
            throw new IllegalArgumentException("O catch-up máximo deve ser positivo");
        }
        if (maximumCatchUp.compareTo(maximumStep) < 0) {
            throw new IllegalArgumentException(
                    "O catch-up máximo não pode ser menor que o passo"
            );
        }
    }

    public static ThermalSimulationSettings livingWorldDefaults() {
        return new ThermalSimulationSettings(
                Duration.ofSeconds(1),
                Duration.ofSeconds(5)
        );
    }
}
