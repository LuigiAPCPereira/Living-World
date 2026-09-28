package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerThermalStateTest {
    @Test
    void ajusteMantemCargaNosLimites() {
        assertEquals(
                1.0D,
                new PlayerThermalState(0.9D).adjustedBy(0.5D).thermalLoad()
        );
        assertEquals(
                -1.0D,
                new PlayerThermalState(-0.9D).adjustedBy(-0.5D).thermalLoad()
        );
    }

    @Test
    void rejeitaCargaInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerThermalState(-1.01D));
        assertThrows(IllegalArgumentException.class, () -> new PlayerThermalState(1.01D));
        assertThrows(IllegalArgumentException.class, () -> new PlayerThermalState(Double.NaN));
    }
}
