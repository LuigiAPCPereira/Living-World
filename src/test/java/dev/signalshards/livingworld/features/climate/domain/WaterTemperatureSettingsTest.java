package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class WaterTemperatureSettingsTest {
    @Test
    void rejeitaConfiguracaoInvalida() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaterTemperatureSettings(8.0D, 1.1D, 32.0D, 0.7D, 0.0D, 40.0D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaterTemperatureSettings(8.0D, 0.65D, 0.0D, 0.7D, 0.0D, 40.0D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaterTemperatureSettings(8.0D, 0.65D, 32.0D, 1.1D, 0.0D, 40.0D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaterTemperatureSettings(50.0D, 0.65D, 32.0D, 0.7D, 0.0D, 40.0D)
        );
    }
}
