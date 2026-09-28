package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WetColdAirScenarioTest {
    @Test
    void jogadorMolhadoEsfriaMaisRapidoDepoisDeSairDaAgua() {
        AirThermalExchangePolicy exchangePolicy = new AirThermalExchangePolicy();
        PlayerThermalPolicy thermalPolicy = new PlayerThermalPolicy();
        AmbientTemperature coldAir = new AmbientTemperature(0.0D);

        PlayerThermalState dry = PlayerThermalState.neutral();
        PlayerThermalState wet = PlayerThermalState.neutral();
        for (int second = 0; second < 20; second++) {
            dry = thermalPolicy.advance(
                    dry,
                    exchangePolicy.exchangeRate(
                            dry,
                            coldAir,
                            WetnessState.dry(),
                            AirThermalTransferFactor.baseline()
                    ),
                    Duration.ofSeconds(1)
            );
            wet = thermalPolicy.advance(
                    wet,
                    exchangePolicy.exchangeRate(
                            wet,
                            coldAir,
                            new WetnessState(1.0D),
                            AirThermalTransferFactor.baseline()
                    ),
                    Duration.ofSeconds(1)
            );
        }

        assertTrue(wet.thermalLoad() < dry.thermalLoad());
        assertTrue(Math.abs(wet.thermalLoad()) > Math.abs(dry.thermalLoad()));
    }
}
