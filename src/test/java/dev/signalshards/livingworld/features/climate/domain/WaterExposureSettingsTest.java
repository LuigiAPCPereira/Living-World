package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class WaterExposureSettingsTest {
    @Test
    void rejeitaTuningInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaterExposureSettings(0.0D, 32.0D, 0.35D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaterExposureSettings(0.12D, 0.0D, 0.35D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new WaterExposureSettings(0.12D, 32.0D, -0.01D)
        );
    }
}
