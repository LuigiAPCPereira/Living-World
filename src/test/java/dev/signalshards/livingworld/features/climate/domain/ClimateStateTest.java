package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClimateStateTest {
    @Test
    void limitaAnomaliasSemAcumuloInfinito() {
        ClimateState state = ClimateState.stable()
                .adjustTemperature(10)
                .adjustMoisture(-10)
                .adjustStormPressure(10);

        assertEquals(new ClimateState(2, -2, 2), state);
    }

    @Test
    void rejeitaEstadoCriadoForaDosLimites() {
        assertThrows(IllegalArgumentException.class, () -> new ClimateState(3, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new ClimateState(0, -3, 0));
        assertThrows(IllegalArgumentException.class, () -> new ClimateState(0, 0, 3));
    }
}
