package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.PlayerThermalSnapshot;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store process-local para estado térmico corporal.
 */
public final class InMemoryPlayerThermalStateStore implements PlayerThermalStateStore {
    private final Map<UUID, PlayerThermalSnapshot> states = new ConcurrentHashMap<>();

    @Override
    public Optional<PlayerThermalSnapshot> load(UUID playerId) {
        return Optional.ofNullable(states.get(
                Objects.requireNonNull(playerId, "id do jogador")
        ));
    }

    @Override
    public void save(UUID playerId, PlayerThermalSnapshot snapshot) {
        states.put(
                Objects.requireNonNull(playerId, "id do jogador"),
                Objects.requireNonNull(snapshot, "snapshot térmico")
        );
    }

    @Override
    public void remove(UUID playerId) {
        states.remove(Objects.requireNonNull(playerId, "id do jogador"));
    }

    int size() {
        return states.size();
    }
}
