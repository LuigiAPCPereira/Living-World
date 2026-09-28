package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalPolicy;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalState;
import dev.signalshards.livingworld.features.climate.domain.PrecipitationExposure;
import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import dev.signalshards.livingworld.features.climate.domain.WetnessPolicy;
import dev.signalshards.livingworld.features.climate.domain.WetnessState;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerThermalSimulationTest {
    private final PlayerThermalSimulation simulation = new PlayerThermalSimulation();

    @Test
    void dezSegundosEmUmaChamadaEquivalemADezChamadasDeUmSegundoDentroDoCatchUp() {
        ThermalExchangeContext initial = icyWaterContext();
        PlayerThermalSimulation longWindow = simulationWithCatchUp(Duration.ofSeconds(10));

        ThermalSimulationResult oneCall = longWindow.advance(
                initial,
                Duration.ofSeconds(10)
        );

        ThermalExchangeContext iterative = initial;
        ThermalSimulationResult last = null;
        for (int second = 0; second < 10; second++) {
            last = longWindow.advance(iterative, Duration.ofSeconds(1));
            iterative = iterative.withPlayerState(
                    last.thermalState(),
                    last.wetnessState()
            );
        }

        assertEquals(
                oneCall.thermalState().thermalLoad(),
                last.thermalState().thermalLoad(),
                1.0E-9D
        );
        assertEquals(
                oneCall.wetnessState().level(),
                last.wetnessState().level(),
                1.0E-9D
        );
    }

    @Test
    void catchUpDefaultLimitaTrabalhoA5Segundos() {
        ThermalSimulationResult result = simulation.advance(
                icyWaterContext(),
                Duration.ofSeconds(30)
        );

        assertEquals(Duration.ofSeconds(5), result.simulatedDuration());
        assertEquals(Duration.ofSeconds(25), result.discardedDuration());
    }

    @Test
    void zeroNaoAlteraEstadoENegativoFalha() {
        ThermalExchangeContext initial = icyWaterContext();
        ThermalSimulationResult zero = simulation.advance(initial, Duration.ZERO);

        assertEquals(initial.thermalState(), zero.thermalState());
        assertEquals(initial.wetness(), zero.wetnessState());
        assertThrows(
                IllegalArgumentException.class,
                () -> simulation.advance(initial, Duration.ofMillis(-1))
        );
    }

    @Test
    void aguaGeladaAvancaWetnessEFrioProgressivamente() {
        ThermalSimulationResult result = simulation.advance(
                icyWaterContext(),
                Duration.ofSeconds(5)
        );

        assertTrue(result.wetnessState().level() > 0.0D);
        assertTrue(result.thermalState().thermalLoad() < 0.0D);
    }

    @Test
    void jogadorMolhadoSecaProgressivamenteForaDaAgua() {
        ThermalExchangeContext context = new ThermalExchangeContext(
                PlayerThermalState.neutral(),
                new WetnessState(1.0D),
                new AmbientTemperature(15.0D),
                WaterExposure.none(),
                PrecipitationExposure.none(),
                PlayerActivity.RESTING,
                new WindExposure(0.50D),
                ShelterFactor.exposed(),
                ArmorThermalLoadout.empty(),
                List.of()
        );

        ThermalSimulationResult result = simulation.advance(
                context,
                Duration.ofSeconds(5)
        );

        assertTrue(result.wetnessState().level() < 1.0D);
        assertTrue(result.wetnessState().level() > 0.0D);
    }

    @Test
    void chuvaExpostaMolhaJogadorSeco() {
        ThermalExchangeContext context = new ThermalExchangeContext(
                PlayerThermalState.neutral(),
                WetnessState.dry(),
                new AmbientTemperature(15.0D),
                WaterExposure.none(),
                new PrecipitationExposure(1.0D),
                PlayerActivity.RESTING,
                WindExposure.calm(),
                ShelterFactor.exposed(),
                ArmorThermalLoadout.empty(),
                List.of()
        );

        ThermalSimulationResult result = simulation.advance(
                context,
                Duration.ofSeconds(5)
        );

        assertTrue(result.wetnessState().level() > 0.0D);
    }

    private PlayerThermalSimulation simulationWithCatchUp(Duration catchUp) {
        return new PlayerThermalSimulation(
                new ThermalExchangeComposer(),
                new PlayerThermalPolicy(),
                new WetnessPolicy(),
                new ThermalSimulationSettings(Duration.ofSeconds(1), catchUp)
        );
    }

    private ThermalExchangeContext icyWaterContext() {
        return new ThermalExchangeContext(
                PlayerThermalState.neutral(),
                WetnessState.dry(),
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
