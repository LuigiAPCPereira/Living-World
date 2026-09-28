package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivityThermalScenarioTest {
    @Test
    void sprintAjudaNoFrioMasNaoSubstituiAbrigoEmFrioExtremo() {
        AirThermalExchangePolicy airPolicy = new AirThermalExchangePolicy();
        ActivityThermalPolicy activityPolicy = new ActivityThermalPolicy();
        AmbientTemperature extremeCold = new AmbientTemperature(-20.0D);

        ThermalExchangeRate resting = airPolicy.exchangeRate(
                PlayerThermalState.neutral(),
                extremeCold,
                WetnessState.dry(),
                AirThermalTransferFactor.baseline()
        ).plus(activityPolicy.exchangeRate(PlayerActivity.RESTING));

        ThermalExchangeRate sprinting = airPolicy.exchangeRate(
                PlayerThermalState.neutral(),
                extremeCold,
                WetnessState.dry(),
                AirThermalTransferFactor.baseline()
        ).plus(activityPolicy.exchangeRate(PlayerActivity.SPRINTING));

        assertTrue(resting.loadPerSecond() < 0.0D);
        assertTrue(sprinting.loadPerSecond() < 0.0D);
        assertTrue(Math.abs(sprinting.loadPerSecond()) < Math.abs(resting.loadPerSecond()));
    }

    @Test
    void nadarEmAguaGeladaAindaEsfriaApesarDoEsforco() {
        WaterThermalExchangePolicy waterPolicy = new WaterThermalExchangePolicy();
        ActivityThermalPolicy activityPolicy = new ActivityThermalPolicy();

        ThermalExchangeRate net = waterPolicy.exchangeRate(
                PlayerThermalState.neutral(),
                new WaterTemperature(0.0D),
                new WaterThermalTransferFactor(1.0D)
        ).plus(activityPolicy.exchangeRate(PlayerActivity.SWIMMING));

        assertTrue(net.loadPerSecond() < 0.0D);
    }

    @Test
    void sprintEmCalorAumentaGanhoTermicoLiquido() {
        AirThermalExchangePolicy airPolicy = new AirThermalExchangePolicy();
        ActivityThermalPolicy activityPolicy = new ActivityThermalPolicy();
        AmbientTemperature hot = new AmbientTemperature(40.0D);

        ThermalExchangeRate resting = airPolicy.exchangeRate(
                PlayerThermalState.neutral(),
                hot,
                WetnessState.dry(),
                AirThermalTransferFactor.baseline()
        );
        ThermalExchangeRate sprinting = resting.plus(
                activityPolicy.exchangeRate(PlayerActivity.SPRINTING)
        );

        assertTrue(sprinting.loadPerSecond() > resting.loadPerSecond());
    }
}
