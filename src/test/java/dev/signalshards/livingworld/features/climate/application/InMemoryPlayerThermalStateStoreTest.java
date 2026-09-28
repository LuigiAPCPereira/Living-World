package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.PlayerThermalSnapshot;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalState;
import dev.signalshards.livingworld.features.climate.domain.WetnessState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryPlayerThermalStateStoreTest {
    @Test
    void jogadorAusenteNaoEhCriadoImplicitamente() {
        InMemoryPlayerThermalStateStore store = new InMemoryPlayerThermalStateStore();

        assertTrue(store.load(UUID.randomUUID()).isEmpty());
        assertEquals(0, store.size());
    }

    @Test
    void salvaCarregaERemoveSnapshot() {
        InMemoryPlayerThermalStateStore store = new InMemoryPlayerThermalStateStore();
        UUID playerId = UUID.randomUUID();
        PlayerThermalSnapshot snapshot = new PlayerThermalSnapshot(
                new PlayerThermalState(-0.35D),
                new WetnessState(0.60D)
        );

        store.save(playerId, snapshot);
        assertEquals(snapshot, store.load(playerId).orElseThrow());
        assertEquals(1, store.size());

        store.remove(playerId);
        assertFalse(store.load(playerId).isPresent());
        assertEquals(0, store.size());
    }
}
