package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.PlayerThermalSnapshot;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestra estado corporal runtime sem conhecer Paper.
 *
 * <p>Fluxo: load snapshot -> combinar com ambiente -> simular -> save snapshot.
 * Chamadas para o mesmo jogador devem ser serializadas pelo owner runtime.</p>
 */
public final class PlayerThermalRuntimeService {
    private final PlayerThermalStateStore store;
    private final PlayerThermalSimulation simulation;

    public PlayerThermalRuntimeService() {
        this(
                new InMemoryPlayerThermalStateStore(),
                new PlayerThermalSimulation()
        );
    }

    public PlayerThermalRuntimeService(
            PlayerThermalStateStore store,
            PlayerThermalSimulation simulation
    ) {
        this.store = Objects.requireNonNull(store, "store térmico");
        this.simulation = Objects.requireNonNull(simulation, "simulação térmica");
    }

    public ThermalSimulationResult advance(
            UUID playerId,
            ThermalEnvironmentContext environment,
            Duration elapsed
    ) {
        Objects.requireNonNull(playerId, "id do jogador");
        Objects.requireNonNull(environment, "contexto ambiental");
        PlayerThermalSnapshot current = store.load(playerId)
                .orElseGet(PlayerThermalSnapshot::neutral);
        ThermalSimulationResult result = simulation.advance(
                ThermalExchangeContext.from(current, environment),
                elapsed
        );
        store.save(
                playerId,
                new PlayerThermalSnapshot(
                        result.thermalState(),
                        result.wetnessState()
                )
        );
        return result;
    }

    public Optional<PlayerThermalSnapshot> snapshot(UUID playerId) {
        return store.load(Objects.requireNonNull(playerId, "id do jogador"));
    }

    public void reset(UUID playerId) {
        store.remove(Objects.requireNonNull(playerId, "id do jogador"));
    }
}
