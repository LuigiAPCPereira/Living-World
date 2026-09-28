package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WetnessEnvironmentPolicyTest {
    private final WetnessEnvironmentPolicy policy = new WetnessEnvironmentPolicy();

    @Test
    void jogadorMolhadoSecaForaDeAguaEChuva() {
        WetnessRate rate = policy.exchangeRate(
                WetnessRate.neutral(),
                PrecipitationExposure.none(),
                new AmbientTemperature(15.0D),
                new WindExposure(0.50D),
                ThermalExchangeRate.neutral(),
                new WetnessState(1.0D)
        );

        assertTrue(rate.levelPerSecond() < 0.0D);
    }

    @Test
    void jogadorJaSecoNaoProduzTaxaNegativaInutil() {
        WetnessRate rate = policy.exchangeRate(
                WetnessRate.neutral(),
                PrecipitationExposure.none(),
                new AmbientTemperature(20.0D),
                WindExposure.calm(),
                ThermalExchangeRate.neutral(),
                WetnessState.dry()
        );

        assertEquals(0.0D, rate.levelPerSecond(), 1.0E-9D);
    }

    @Test
    void chuvaExpostaMolhaApesarDaEvaporacao() {
        WetnessRate rate = policy.exchangeRate(
                WetnessRate.neutral(),
                new PrecipitationExposure(1.0D),
                new AmbientTemperature(15.0D),
                new WindExposure(0.30D),
                ThermalExchangeRate.neutral(),
                WetnessState.dry()
        );

        assertTrue(rate.levelPerSecond() > 0.0D);
    }

    @Test
    void calorLocalAceleraSecagem() {
        WetnessRate withoutHeat = policy.exchangeRate(
                WetnessRate.neutral(),
                PrecipitationExposure.none(),
                new AmbientTemperature(15.0D),
                new WindExposure(0.30D),
                ThermalExchangeRate.neutral(),
                new WetnessState(1.0D)
        );
        WetnessRate withHeat = policy.exchangeRate(
                WetnessRate.neutral(),
                PrecipitationExposure.none(),
                new AmbientTemperature(15.0D),
                new WindExposure(0.30D),
                new ThermalExchangeRate(0.010D),
                new WetnessState(1.0D)
        );

        assertTrue(withHeat.levelPerSecond() < withoutHeat.levelPerSecond());
    }

    @Test
    void precipitacaoFriaMolhaMaisDevagarQueChuvaMorna() {
        WetnessRate cold = policy.exchangeRate(
                WetnessRate.neutral(),
                new PrecipitationExposure(1.0D),
                new AmbientTemperature(-5.0D),
                WindExposure.calm(),
                ThermalExchangeRate.neutral(),
                WetnessState.dry()
        );
        WetnessRate warm = policy.exchangeRate(
                WetnessRate.neutral(),
                new PrecipitationExposure(1.0D),
                new AmbientTemperature(10.0D),
                WindExposure.calm(),
                ThermalExchangeRate.neutral(),
                WetnessState.dry()
        );

        assertTrue(cold.levelPerSecond() < warm.levelPerSecond());
        assertTrue(cold.levelPerSecond() > 0.0D);
    }
}
