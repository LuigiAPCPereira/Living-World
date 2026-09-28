package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaterThermalExchangePolicyTest {
    private final WaterThermalExchangePolicy policy = new WaterThermalExchangePolicy();

    @Test
    void semContatoNaoHaTrocaMesmoComAguaExtrema() {
        ThermalExchangeRate rate = policy.exchangeRate(
                PlayerThermalState.neutral(),
                new WaterTemperature(0.0D),
                WaterThermalTransferFactor.neutral()
        );

        assertEquals(0.0D, rate.loadPerSecond());
    }

    @Test
    void aguaMuitoFriaEsfriaEstadoNeutro() {
        ThermalExchangeRate rate = policy.exchangeRate(
                PlayerThermalState.neutral(),
                new WaterTemperature(0.0D),
                new WaterThermalTransferFactor(1.0D)
        );

        assertTrue(rate.loadPerSecond() < 0.0D);
        assertEquals(-0.08D, rate.loadPerSecond(), 1.0E-9D);
    }

    @Test
    void aguaQuenteAqueceEstadoNeutro() {
        ThermalExchangeRate rate = policy.exchangeRate(
                PlayerThermalState.neutral(),
                new WaterTemperature(40.0D),
                new WaterThermalTransferFactor(1.0D)
        );

        assertTrue(rate.loadPerSecond() > 0.0D);
        assertEquals(0.032D, rate.loadPerSecond(), 1.0E-9D);
    }

    @Test
    void aguaFriaPodeAquecerJogadorAindaMaisFrioQueSeuEquilibrio() {
        ThermalExchangeRate rate = policy.exchangeRate(
                new PlayerThermalState(-0.90D),
                new WaterTemperature(20.0D),
                new WaterThermalTransferFactor(1.0D)
        );

        assertTrue(rate.loadPerSecond() > 0.0D);
        assertEquals(0.024D, rate.loadPerSecond(), 1.0E-9D);
    }

    @Test
    void maiorContatoAumentaMagnitudeDaTroca() {
        ThermalExchangeRate partial = policy.exchangeRate(
                PlayerThermalState.neutral(),
                new WaterTemperature(0.0D),
                new WaterThermalTransferFactor(0.25D)
        );
        ThermalExchangeRate full = policy.exchangeRate(
                PlayerThermalState.neutral(),
                new WaterTemperature(0.0D),
                new WaterThermalTransferFactor(1.0D)
        );

        assertTrue(Math.abs(partial.loadPerSecond()) < Math.abs(full.loadPerSecond()));
    }

    @Test
    void taxaFinalPermaneceBounded() {
        WaterThermalExchangePolicy aggressive = new WaterThermalExchangePolicy(
                new WaterThermalExchangeSettings(32.0D, 20.0D, 10.0D, 0.12D)
        );
        ThermalExchangeRate rate = aggressive.exchangeRate(
                new PlayerThermalState(1.0D),
                new WaterTemperature(0.0D),
                new WaterThermalTransferFactor(10.0D)
        );

        assertEquals(-0.12D, rate.loadPerSecond(), 1.0E-9D);
    }
}
