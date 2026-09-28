package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.PlayerThermalState;
import dev.signalshards.livingworld.features.climate.domain.WetnessState;

import java.time.Duration;
import java.util.Objects;

/**
 * Estado resultante de uma janela temporal bounded.
 */
public record ThermalSimulationResult(
        PlayerThermalState thermalState,
        WetnessState wetnessState,
        Duration simulatedDuration,
        Duration discardedDuration
) {
    public ThermalSimulationResult {
        Objects.requireNonNull(thermalState, "estado térmico");
        Objects.requireNonNull(wetnessState, "wetness");
        Objects.requireNonNull(simulatedDuration, "duração simulada");
        Objects.requireNonNull(discardedDuration, "duração descartada");
        if (simulatedDuration.isNegative() || discardedDuration.isNegative()) {
            throw new IllegalArgumentException(
                    "Durações do resultado não podem ser negativas"
            );
        }
    }
}
