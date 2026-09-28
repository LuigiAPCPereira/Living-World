package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectThermalExposurePolicyTest {
    private final DirectThermalExposurePolicy policy =
            new DirectThermalExposurePolicy();

    @Test
    void noneEhNeutro() {
        assertEquals(
                0.0D,
                policy.exchangeRate(DirectThermalExposure.NONE).loadPerSecond(),
                1.0E-9D
        );
    }

    @Test
    void lavaAqueceMaisQueFogoEPowderSnowEsfria() {
        double fire = policy.exchangeRate(DirectThermalExposure.FIRE).loadPerSecond();
        double lava = policy.exchangeRate(DirectThermalExposure.LAVA).loadPerSecond();
        double powder = policy.exchangeRate(
                DirectThermalExposure.POWDER_SNOW
        ).loadPerSecond();

        assertTrue(fire > 0.0D);
        assertTrue(lava > fire);
        assertTrue(powder < 0.0D);
    }
}
