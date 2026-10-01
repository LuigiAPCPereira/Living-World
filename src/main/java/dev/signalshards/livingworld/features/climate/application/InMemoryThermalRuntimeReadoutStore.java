package dev.signalshards.livingworld.features.climate.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryThermalRuntimeReadoutStore
        implements ThermalRuntimeReadoutStore {
    private final Map<UUID, ThermalRuntimeReadout> readouts = new HashMap<>();

    @Override
    public Optional<ThermalRuntimeReadout> load(UUID playerId) {
        return Optional.ofNullable(
                readouts.get(Objects.requireNonNull(playerId, "id do jogador"))
        );
    }

    @Override
    public void save(UUID playerId, ThermalRuntimeReadout readout) {
        readouts.put(
                Objects.requireNonNull(playerId, "id do jogador"),
                Objects.requireNonNull(readout, "readout térmico")
        );
    }

    @Override
    public void remove(UUID playerId) {
        readouts.remove(Objects.requireNonNull(playerId, "id do jogador"));
    }

    @Override
    public void clear() {
        readouts.clear();
    }
}
