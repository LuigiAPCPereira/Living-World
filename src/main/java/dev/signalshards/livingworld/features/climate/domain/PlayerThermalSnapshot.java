package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Snapshot corporal de curto prazo usado pelo runtime térmico.
 */
public record PlayerThermalSnapshot(
        PlayerThermalState thermalState,
        WetnessState wetnessState
) {
    public PlayerThermalSnapshot {
        Objects.requireNonNull(thermalState, "estado térmico");
        Objects.requireNonNull(wetnessState, "wetness");
    }

    public static PlayerThermalSnapshot neutral() {
        return new PlayerThermalSnapshot(
                PlayerThermalState.neutral(),
                WetnessState.dry()
        );
    }
}
