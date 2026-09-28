package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WindShelterScenarioTest {
    @Test
    void abrigoProtegeMaisQuePicoVentosoNoMesmoFrio() {
        AirTransferPolicy transferPolicy = new AirTransferPolicy();
        AirThermalExchangePolicy exchangePolicy = new AirThermalExchangePolicy();
        PlayerThermalPolicy thermalPolicy = new PlayerThermalPolicy();
        AmbientTemperature ambient = new AmbientTemperature(-5.0D);
        WetnessState wet = new WetnessState(1.0D);

        AirThermalTransferFactor windyPeak = transferPolicy.factorFor(
                new WindExposure(1.0D),
                ShelterFactor.exposed()
        );
        AirThermalTransferFactor sheltered = transferPolicy.factorFor(
                WindExposure.calm(),
                new ShelterFactor(1.0D)
        );

        PlayerThermalState peakState = PlayerThermalState.neutral();
        PlayerThermalState shelterState = PlayerThermalState.neutral();
        for (int second = 0; second < 20; second++) {
            peakState = thermalPolicy.advance(
                    peakState,
                    exchangePolicy.exchangeRate(peakState, ambient, wet, windyPeak),
                    Duration.ofSeconds(1)
            );
            shelterState = thermalPolicy.advance(
                    shelterState,
                    exchangePolicy.exchangeRate(shelterState, ambient, wet, sheltered),
                    Duration.ofSeconds(1)
            );
        }

        assertTrue(peakState.thermalLoad() < shelterState.thermalLoad());
        assertTrue(Math.abs(peakState.thermalLoad()) > Math.abs(shelterState.thermalLoad()));
    }
}
