package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WinterThermalPhasePolicyTest {
    private final PhysicalWinterSettings settings =
            PhysicalWinterSettings.defaults();
    private final WinterThermalPhasePolicy policy =
            new WinterThermalPhasePolicy(settings);

    @Test
    void histereseSeparaFreezeHoldEThaw() {
        assertEquals(
                WinterThermalPhase.FREEZING,
                policy.phaseFor(new AmbientTemperature(-5.0D))
        );
        assertEquals(
                WinterThermalPhase.FREEZING,
                policy.phaseFor(new AmbientTemperature(-1.0D))
        );
        assertEquals(
                WinterThermalPhase.HOLDING,
                policy.phaseFor(new AmbientTemperature(0.5D))
        );
        assertEquals(
                WinterThermalPhase.THAWING,
                policy.phaseFor(new AmbientTemperature(2.0D))
        );
        assertEquals(
                WinterThermalPhase.THAWING,
                policy.phaseFor(new AmbientTemperature(8.0D))
        );
    }

    @Test
    void settingsExigemFaixaDeHistereseValida() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PhysicalWinterSettings(2.0D, 2.0D, 32)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new PhysicalWinterSettings(3.0D, 2.0D, 32)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new PhysicalWinterSettings(-1.0D, 2.0D, 0)
        );
    }
}
