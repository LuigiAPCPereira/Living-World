package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerThermalThresholdsTest {
    @Test
    void exigeLimitesOrdenadosEInternosAoIndice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayerThermalThresholds(
                        -0.65D, -0.45D, -0.30D, -0.20D, -0.10D,
                        0.10D, 0.25D, 0.20D, 0.65D
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayerThermalThresholds(
                        -1.0D, -0.45D, -0.30D, -0.20D, -0.10D,
                        0.10D, 0.25D, 0.45D, 0.65D
                )
        );
    }
}
