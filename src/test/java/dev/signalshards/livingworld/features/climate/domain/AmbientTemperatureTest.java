package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AmbientTemperatureTest {
    @Test
    void aceitaValorFinito() {
        assertEquals(-12.5D, new AmbientTemperature(-12.5D).degreesCelsius());
    }

    @Test
    void rejeitaValorNaoFinito() {
        assertThrows(IllegalArgumentException.class, () -> new AmbientTemperature(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new AmbientTemperature(Double.POSITIVE_INFINITY));
    }
}
