package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AirThermalExchangeSettingsTest {
    @Test
    void rejeitaConfiguracaoInvalida() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AirThermalExchangeSettings(20.0D, 0.0D, 0.015D, 2.5D, 0.06D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new AirThermalExchangeSettings(20.0D, 25.0D, 0.0D, 2.5D, 0.06D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new AirThermalExchangeSettings(20.0D, 25.0D, 0.015D, 0.9D, 0.06D)
        );
    }
}
