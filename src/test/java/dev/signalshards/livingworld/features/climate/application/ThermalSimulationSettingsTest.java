package dev.signalshards.livingworld.features.climate.application;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ThermalSimulationSettingsTest {
    @Test
    void exigePassosECatchUpPositivosEOrdenados() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ThermalSimulationSettings(
                        Duration.ZERO,
                        Duration.ofSeconds(5)
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ThermalSimulationSettings(
                        Duration.ofSeconds(2),
                        Duration.ofSeconds(1)
                )
        );
    }
}
