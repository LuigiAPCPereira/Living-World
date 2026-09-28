package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.ClimateSnapshot;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;
import dev.signalshards.livingworld.features.climate.domain.WeatherTendency;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SeasonalLeafVisualPolicyTest {
    private final NaturalGrowthSuitabilityPolicy growthPolicy =
            new NaturalGrowthSuitabilityPolicy();

    @Test
    void estacaoSelecionaComportamentoVisual() {
        SeasonalLeafVisualPolicy policy = policyWithTreeMultiplier(1.0D);
        ClimateSnapshot climate = snapshot(
                ThermalBand.TEMPERADO,
                MoistureBand.EQUILIBRADO
        );

        assertEquals(
                SeasonalLeafVisualPolicy.Effect.SPRING,
                policy.select(climate, Season.PRIMAVERA, 0.0D)
        );
        assertEquals(
                SeasonalLeafVisualPolicy.Effect.AUTUMN,
                policy.select(climate, Season.OUTONO, 0.0D)
        );
        assertEquals(
                SeasonalLeafVisualPolicy.Effect.NONE,
                policy.select(climate, Season.VERAO, 0.0D)
        );
    }

    @Test
    void limiarDaChanceEcologicaEhExclusivo() {
        SeasonalLeafVisualPolicy policy = policyWithTreeMultiplier(1.0D);
        ClimateSnapshot climate = snapshot(
                ThermalBand.TEMPERADO,
                MoistureBand.EQUILIBRADO
        );

        assertEquals(
                SeasonalLeafVisualPolicy.Effect.SPRING,
                policy.select(climate, Season.PRIMAVERA, Math.nextDown(0.35D))
        );
        assertEquals(
                SeasonalLeafVisualPolicy.Effect.NONE,
                policy.select(climate, Season.PRIMAVERA, 0.35D)
        );
    }

    @Test
    void chanceVisualUsaMultiplicadorDeArvores() {
        SeasonalLeafVisualPolicy policy = policyWithTreeMultiplier(0.50D);
        ClimateSnapshot climate = snapshot(
                ThermalBand.TEMPERADO,
                MoistureBand.EQUILIBRADO
        );

        assertEquals(
                SeasonalLeafVisualPolicy.Effect.SPRING,
                policy.select(climate, Season.PRIMAVERA, Math.nextDown(0.175D))
        );
        assertEquals(
                SeasonalLeafVisualPolicy.Effect.NONE,
                policy.select(climate, Season.PRIMAVERA, 0.175D)
        );
    }

    @Test
    void neveExigeFrioEUmidadeCompativel() {
        SeasonalLeafVisualPolicy policy = policyWithTreeMultiplier(1.0D);

        assertEquals(
                SeasonalLeafVisualPolicy.Effect.SNOW,
                policy.select(
                        snapshot(ThermalBand.FRIO, MoistureBand.EQUILIBRADO),
                        Season.INVERNO,
                        0.0D
                )
        );
        assertEquals(
                SeasonalLeafVisualPolicy.Effect.SNOW,
                policy.select(
                        snapshot(ThermalBand.CONGELANTE, MoistureBand.UMIDO),
                        Season.INVERNO,
                        0.0D
                )
        );
        assertEquals(
                SeasonalLeafVisualPolicy.Effect.NONE,
                policy.select(
                        snapshot(ThermalBand.TEMPERADO, MoistureBand.UMIDO),
                        Season.INVERNO,
                        0.0D
                )
        );
        assertEquals(
                SeasonalLeafVisualPolicy.Effect.NONE,
                policy.select(
                        snapshot(ThermalBand.FRIO, MoistureBand.SECO),
                        Season.INVERNO,
                        0.0D
                )
        );
    }

    @Test
    void reutilizaChanceExistenteEmTodasAsFaixasSemDuplicarFatores() {
        var modifier = new DefaultSeasonalEcologyModifier();
        var policy = new SeasonalLeafVisualPolicy(growthPolicy, modifier);
        for (ThermalBand thermal : ThermalBand.values()) {
            for (MoistureBand moisture : MoistureBand.values()) {
                var climate = snapshot(thermal, moisture);
                double chance = 0.35D * Math.min(1.0D, growthPolicy.acceptanceChance(
                        climate, Season.OUTONO, GrowthCategory.TREE, 1.0D, modifier));
                assertEquals(SeasonalLeafVisualPolicy.Effect.AUTUMN,
                        policy.select(climate, Season.OUTONO, Math.nextDown(chance)));
                assertEquals(SeasonalLeafVisualPolicy.Effect.NONE,
                        policy.select(climate, Season.OUTONO, chance));
            }
        }
        var cold = snapshot(ThermalBand.CONGELANTE, MoistureBand.UMIDO);
        assertEquals(SeasonalLeafVisualPolicy.Effect.SNOW,
                policy.select(cold, Season.INVERNO, 0.0D));
        assertEquals(SeasonalLeafVisualPolicy.Effect.NONE,
                policy.select(cold, Season.VERAO, 0.0D));
    }

    @Test
    void rejeitaAmostraForaDoIntervalo() {
        SeasonalLeafVisualPolicy policy = policyWithTreeMultiplier(1.0D);
        ClimateSnapshot climate = snapshot(
                ThermalBand.TEMPERADO,
                MoistureBand.EQUILIBRADO
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> policy.select(climate, Season.PRIMAVERA, -0.01D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.select(climate, Season.PRIMAVERA, 1.0D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.select(climate, Season.PRIMAVERA, Double.NaN)
        );
    }

    private SeasonalLeafVisualPolicy policyWithTreeMultiplier(double treeMultiplier) {
        SeasonalEcologyModifier modifier = new SeasonalEcologyModifier() {
            @Override
            public double cropMultiplier(Season season) {
                return 0.0D;
            }

            @Override
            public double treeMultiplier(Season season) {
                return treeMultiplier;
            }

            @Override
            public double grassMultiplier(Season season) {
                return 0.0D;
            }
        };
        return new SeasonalLeafVisualPolicy(growthPolicy, modifier);
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
