package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.PlayerThermalSnapshot;

import java.util.Optional;
import java.util.UUID;

/**
 * Estado corporal runtime por jogador.
 *
 * <p>O contrato é deliberadamente efêmero: nenhuma persistência PDC é exigida
 * e adapters podem remover o estado em quit/respawn.</p>
 */
public interface PlayerThermalStateStore {
    Optional<PlayerThermalSnapshot> load(UUID playerId);

    void save(UUID playerId, PlayerThermalSnapshot snapshot);

    void remove(UUID playerId);
}
