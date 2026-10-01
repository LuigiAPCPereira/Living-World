package dev.signalshards.livingworld.features.climate.application;

import java.util.Optional;
import java.util.UUID;

public interface ThermalRuntimeReadoutStore {
    Optional<ThermalRuntimeReadout> load(UUID playerId);

    void save(UUID playerId, ThermalRuntimeReadout readout);

    void remove(UUID playerId);

    void clear();
}
