package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalHeatThermalScenarioTest {
    private final AirThermalExchangePolicy airPolicy = new AirThermalExchangePolicy();
    private final LocalHeatSourcePolicy heatPolicy = new LocalHeatSourcePolicy();
    private final PlayerThermalPolicy thermalPolicy = new PlayerThermalPolicy();

    @Test
    void umaTochaAjudaMasNaoResolveFrioExtremoAoArLivre() {
        ThermalExchangeRate air = airPolicy.exchangeRate(
                PlayerThermalState.neutral(),
                new AmbientTemperature(-20.0D),
                WetnessState.dry(),
                AirThermalTransferFactor.baseline()
        );
        ThermalExchangeRate torch = heatPolicy.exchangeRate(List.of(
                new LocalHeatExposure(LocalHeatSourceType.TORCH, 0.0D, 1.0D, 1.0D)
        ));

        ThermalExchangeRate net = air.plus(torch);
        assertTrue(net.loadPerSecond() < 0.0D);
        assertTrue(Math.abs(net.loadPerSecond()) < Math.abs(air.loadPerSecond()));
    }

    @Test
    void fogueiraPodeRecuperarJogadorQuandoHaAbrigo() {
        AirThermalTransferFactor sheltered = new AirTransferPolicy().factorFor(
                WindExposure.calm(),
                new ShelterFactor(1.0D)
        );
        ThermalExchangeRate air = airPolicy.exchangeRate(
                new PlayerThermalState(-0.40D),
                new AmbientTemperature(-10.0D),
                WetnessState.dry(),
                sheltered
        );
        ThermalExchangeRate campfire = heatPolicy.exchangeRate(List.of(
                new LocalHeatExposure(LocalHeatSourceType.CAMPFIRE, 0.0D, 1.0D, 1.0D)
        ));

        assertTrue(air.plus(campfire).loadPerSecond() > 0.0D);
    }

    @Test
    void exposicaoProlongadaALavaAcumulaSuperaquecimento() {
        PlayerThermalState state = PlayerThermalState.neutral();
        ThermalExchangeRate lava = heatPolicy.exchangeRate(List.of(
                new LocalHeatExposure(LocalHeatSourceType.LAVA, 0.0D, 1.0D, 1.0D)
        ));

        for (int second = 0; second < 40; second++) {
            state = thermalPolicy.advance(state, lava, Duration.ofSeconds(1));
        }

        assertEquals(PlayerThermalBand.OVERHEATING, thermalPolicy.bandFor(state));
    }
}
