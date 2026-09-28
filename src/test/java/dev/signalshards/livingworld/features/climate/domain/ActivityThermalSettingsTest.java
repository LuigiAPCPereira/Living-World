package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ActivityThermalSettingsTest {
    @Test
    void rejeitaTaxaNegativaOuAcimaDoLimite() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ActivityThermalSettings(
                        -0.001D, 0.004D, 0.005D, 0.003D, 0.0005D, 0.006D
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ActivityThermalSettings(
                        0.0015D, 0.007D, 0.005D, 0.003D, 0.0005D, 0.006D
                )
        );
    }
}
