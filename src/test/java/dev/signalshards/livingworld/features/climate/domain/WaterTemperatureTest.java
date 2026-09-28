package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WaterTemperatureTest {
    @Test
    void aceitaTemperaturaFinita() {
        assertEquals(4.0D, new WaterTemperature(4.0D).degreesCelsius());
    }

    @Test
    void rejeitaTemperaturaNaoFinita() {
        assertThrows(IllegalArgumentException.class, () -> new WaterTemperature(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new WaterTemperature(Double.POSITIVE_INFINITY));
    }
}
