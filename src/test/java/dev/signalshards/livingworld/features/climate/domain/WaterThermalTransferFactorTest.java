package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WaterThermalTransferFactorTest {
    @Test
    void zeroRepresentaAusenciaDeTransferenciaPorAgua() {
        assertEquals(0.0D, WaterThermalTransferFactor.neutral().value());
    }

    @Test
    void rejeitaValorNegativoOuNaoFinito() {
        assertThrows(IllegalArgumentException.class, () -> new WaterThermalTransferFactor(-0.01D));
        assertThrows(IllegalArgumentException.class, () -> new WaterThermalTransferFactor(Double.NaN));
    }
}
