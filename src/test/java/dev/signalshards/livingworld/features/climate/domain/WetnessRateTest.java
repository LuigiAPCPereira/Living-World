package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WetnessRateTest {
    @Test
    void zeroRepresentaEstadoNeutro() {
        assertEquals(0.0D, WetnessRate.neutral().levelPerSecond());
    }

    @Test
    void rejeitaTaxaNaoFinita() {
        assertThrows(IllegalArgumentException.class, () -> new WetnessRate(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new WetnessRate(Double.POSITIVE_INFINITY));
    }
}
