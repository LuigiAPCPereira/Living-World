package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerThermalRuntimeServiceTest {
    @Test
    void primeiroAdvanceCriaSnapshotEChamadasSeguintesAcumulam() {
        InMemoryPlayerThermalStateStore store = new InMemoryPlayerThermalStateStore();
        PlayerThermalRuntimeService service = new PlayerThermalRuntimeService(
                store,
                new PlayerThermalSimulation()
        );
        UUID playerId = UUID.randomUUID();
        ThermalEnvironmentContext environment = icyWater();

        ThermalSimulationResult first = service.advance(
                playerId,
                environment,
                Duration.ofSeconds(1)
        );
        ThermalSimulationResult second = service.advance(
                playerId,
                environment,
                Duration.ofSeconds(1)
        );

        assertTrue(service.snapshot(playerId).isPresent());
        assertTrue(second.thermalState().thermalLoad() < first.thermalState().thermalLoad());
        assertTrue(second.wetnessState().level() > first.wetnessState().level());
    }

    @Test
    void resetRemoveEstadoCorporalRuntime() {
        PlayerThermalRuntimeService service = new PlayerThermalRuntimeService();
        UUID playerId = UUID.randomUUID();
        service.advance(playerId, icyWater(), Duration.ofSeconds(1));

        service.reset(playerId);

        assertFalse(service.snapshot(playerId).isPresent());
    }

    private ThermalEnvironmentContext icyWater() {
        return new ThermalEnvironmentContext(
                new AmbientTemperature(-10.0D),
                new WaterExposure(1.0D, 0.0D),
                PlayerActivity.RESTING,
                WindExposure.calm(),
                ShelterFactor.exposed(),
                ArmorThermalLoadout.empty(),
                List.of()
        );
    }
}
