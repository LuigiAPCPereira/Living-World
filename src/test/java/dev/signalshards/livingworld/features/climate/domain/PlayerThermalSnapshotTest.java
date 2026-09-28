package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerThermalSnapshotTest {
    @Test
    void neutralComecaConfortavelESeco() {
        PlayerThermalSnapshot snapshot = PlayerThermalSnapshot.neutral();

        assertEquals(0.0D, snapshot.thermalState().thermalLoad());
        assertEquals(0.0D, snapshot.wetnessState().level());
    }
}
