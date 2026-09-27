package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.ClimateSnapshot;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;
import dev.signalshards.livingworld.features.climate.domain.WeatherTendency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NaturalGrowthSuitabilityPolicyTest {
    private final NaturalGrowthSuitabilityPolicy policy =
            new NaturalGrowthSuitabilityPolicy();

    @Test
    void climaIdealMantemCrescimentoVanilla() {
        ClimateSnapshot climate = snapshot(
                ThermalBand.TEMPERADO,
                MoistureBand.EQUILIBRADO
        );

        assertEquals(1.0D, policy.acceptanceChance(climate, 1.0D));
    }

    @Test
    void strengthZeroDesligaImpactoSemTrocarPolitica() {
        ClimateSnapshot climate = snapshot(
                ThermalBand.CONGELANTE,
                MoistureBand.ARIDO
        );

        assertEquals(1.0D, policy.acceptanceChance(climate, 0.0D));
    }

    @Test
    void extremosTermicosEUmidadeReduzemChanceDeFormaComposta() {
        ClimateSnapshot climate = snapshot(
                ThermalBand.CONGELANTE,
                MoistureBand.ARIDO
        );

        assertEquals(0.175D, policy.acceptanceChance(climate, 1.0D), 0.000001D);
        assertEquals(0.46375D, policy.acceptanceChance(climate, 0.65D), 0.000001D);
    }

    @Test
    void rejeitaForcaForaDoIntervalo() {
        ClimateSnapshot climate = snapshot(
                ThermalBand.TEMPERADO,
                MoistureBand.EQUILIBRADO
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> policy.acceptanceChance(climate, 1.1D)
        );
    }

    private ClimateSnapshot snapshot(
            ThermalBand temperature,
            MoistureBand moisture
    ) {
        return new ClimateSnapshot(
                temperature,
                moisture,
                WeatherTendency.ESTAVEL
        );
    }
}
