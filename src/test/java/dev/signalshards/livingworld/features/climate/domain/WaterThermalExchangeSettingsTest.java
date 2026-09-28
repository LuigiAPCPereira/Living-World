package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class WaterThermalExchangeSettingsTest {
    @Test
    void rejeitaConfiguracaoInvalida() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaterThermalExchangeSettings(32.0D, 0.0D, 0.08D, 0.12D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaterThermalExchangeSettings(32.0D, 20.0D, 0.0D, 0.12D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaterThermalExchangeSettings(32.0D, 20.0D, 0.08D, 0.0D)
        );
    }
}
