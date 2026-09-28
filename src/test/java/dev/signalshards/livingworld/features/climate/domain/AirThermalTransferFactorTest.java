package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AirThermalTransferFactorTest {
    @Test
    void baselineENoneSaoExplicitos() {
        assertEquals(1.0D, AirThermalTransferFactor.baseline().value());
        assertEquals(0.0D, AirThermalTransferFactor.none().value());
    }

    @Test
    void rejeitaValorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new AirThermalTransferFactor(-0.01D));
        assertThrows(IllegalArgumentException.class, () -> new AirThermalTransferFactor(Double.NaN));
    }
}
