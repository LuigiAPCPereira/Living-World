package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirThermalExchangePolicyTest {
    private final AirThermalExchangePolicy policy = new AirThermalExchangePolicy();

    @Test
    void ambienteNeutroMantemEstadoNeutro() {
        ThermalExchangeRate rate = policy.exchangeRate(
                PlayerThermalState.neutral(),
                new AmbientTemperature(20.0D),
                WetnessState.dry(),
                AirThermalTransferFactor.baseline()
        );

        assertEquals(0.0D, rate.loadPerSecond());
    }

    @Test
    void frioEsfriaECalorAquece() {
        ThermalExchangeRate cold = policy.exchangeRate(
                PlayerThermalState.neutral(),
                new AmbientTemperature(-5.0D),
                WetnessState.dry(),
                AirThermalTransferFactor.baseline()
        );
        ThermalExchangeRate hot = policy.exchangeRate(
                PlayerThermalState.neutral(),
                new AmbientTemperature(45.0D),
                WetnessState.dry(),
                AirThermalTransferFactor.baseline()
        );

        assertTrue(cold.loadPerSecond() < 0.0D);
        assertTrue(hot.loadPerSecond() > 0.0D);
    }

    @Test
    void wetnessAmplificaTrocaSemAlterarDirecao() {
        ThermalExchangeRate dry = policy.exchangeRate(
                PlayerThermalState.neutral(),
                new AmbientTemperature(0.0D),
                WetnessState.dry(),
                AirThermalTransferFactor.baseline()
        );
        ThermalExchangeRate wet = policy.exchangeRate(
                PlayerThermalState.neutral(),
                new AmbientTemperature(0.0D),
                new WetnessState(1.0D),
                AirThermalTransferFactor.baseline()
        );

        assertTrue(dry.loadPerSecond() < 0.0D);
        assertTrue(wet.loadPerSecond() < 0.0D);
        assertTrue(Math.abs(wet.loadPerSecond()) > Math.abs(dry.loadPerSecond()));
    }

    @Test
    void ambienteFrioPodeAquecerEstadoAindaMaisFrio() {
        ThermalExchangeRate rate = policy.exchangeRate(
                new PlayerThermalState(-1.0D),
                new AmbientTemperature(0.0D),
                WetnessState.dry(),
                AirThermalTransferFactor.baseline()
        );

        assertTrue(rate.loadPerSecond() > 0.0D);
    }

    @Test
    void fatorZeroRemoveTrocaComAr() {
        ThermalExchangeRate rate = policy.exchangeRate(
                PlayerThermalState.neutral(),
                new AmbientTemperature(-30.0D),
                new WetnessState(1.0D),
                AirThermalTransferFactor.none()
        );

        assertEquals(0.0D, rate.loadPerSecond());
    }
}
