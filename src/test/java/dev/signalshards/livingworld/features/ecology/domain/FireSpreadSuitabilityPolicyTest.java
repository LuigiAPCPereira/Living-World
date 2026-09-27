package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.ClimateSnapshot;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;
import dev.signalshards.livingworld.features.climate.domain.WeatherTendency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FireSpreadSuitabilityPolicyTest {
    private final FireSpreadSuitabilityPolicy policy =
            new FireSpreadSuitabilityPolicy();

    @Test
    void quenteESecoMantemChanceVanilla() {
        assertEquals(
                1.0D,
                policy.acceptanceChance(
                        snapshot(ThermalBand.QUENTE, MoistureBand.SECO),
                        1.0D
                )
        );
    }

    @Test
    void frioEUmidoReduzChance() {
        double coldWet = policy.acceptanceChance(
                snapshot(ThermalBand.FRIO, MoistureBand.UMIDO),
                1.0D
        );

        assertEquals(0.3575D, coldWet, 0.000001D);
        assertTrue(coldWet < 1.0D);
    }

    @Test
    void strengthZeroMantemVanilla() {
        assertEquals(
                1.0D,
                policy.acceptanceChance(
                        snapshot(ThermalBand.CONGELANTE, MoistureBand.ENCHARCADO),
                        0.0D
                )
        );
    }

    private ClimateSnapshot snapshot(
            ThermalBand thermal,
            MoistureBand moisture
    ) {
        return new ClimateSnapshot(
                thermal,
                moisture,
                WeatherTendency.ESTAVEL
        );
    }
}
