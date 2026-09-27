package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.ClimateSnapshot;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;
import dev.signalshards.livingworld.features.climate.domain.WeatherTendency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FarmlandMoistureRetentionPolicyTest {
    private final FarmlandMoistureRetentionPolicy policy =
            new FarmlandMoistureRetentionPolicy();

    @Test
    void climaFrioEUmidoRetemMaisQueQuenteESeco() {
        double wetCold = policy.retentionChance(
                snapshot(ThermalBand.FRIO, MoistureBand.UMIDO),
                1.0D
        );
        double hotDry = policy.retentionChance(
                snapshot(ThermalBand.QUENTE, MoistureBand.SECO),
                1.0D
        );

        assertTrue(wetCold > hotDry);
        assertEquals(0.55D, wetCold, 0.000001D);
        assertEquals(0.0D, hotDry, 0.000001D);
    }

    @Test
    void strengthZeroDesligaRetencaoExtra() {
        assertEquals(
                0.0D,
                policy.retentionChance(
                        snapshot(ThermalBand.CONGELANTE, MoistureBand.ENCHARCADO),
                        0.0D
                )
        );
    }

    @Test
    void baseNuncaPassaDoTeto() {
        assertEquals(
                0.75D,
                policy.retentionChance(
                        snapshot(ThermalBand.CONGELANTE, MoistureBand.ENCHARCADO),
                        1.0D
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
