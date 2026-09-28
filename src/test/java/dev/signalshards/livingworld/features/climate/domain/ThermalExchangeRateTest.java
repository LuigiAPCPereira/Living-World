package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ThermalExchangeRateTest {
    @Test
    void zeroRepresentaTrocaNeutra() {
        assertEquals(0.0D, ThermalExchangeRate.neutral().loadPerSecond());
    }

    @Test
    void rejeitaTaxaNaoFinita() {
        assertThrows(IllegalArgumentException.class, () -> new ThermalExchangeRate(Double.NaN));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ThermalExchangeRate(Double.NEGATIVE_INFINITY)
        );
    }
}
