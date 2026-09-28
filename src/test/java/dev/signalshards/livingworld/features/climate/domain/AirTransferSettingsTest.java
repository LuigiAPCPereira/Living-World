package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AirTransferSettingsTest {
    @Test
    void rejeitaConfiguracaoInvalida() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AirTransferSettings(-0.1D, 0.75D, 0.2D, 2.5D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new AirTransferSettings(1.0D, 1.1D, 0.2D, 2.5D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new AirTransferSettings(1.0D, 0.75D, 3.0D, 2.5D)
        );
    }
}
