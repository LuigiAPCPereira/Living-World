package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalState;
import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThermalFeedbackCoordinatorTest {
    @Test
    void observeCalculaProfileSemNovoProbeNoPulse() {
        ThermalFeedbackCoordinator coordinator = new ThermalFeedbackCoordinator();
        UUID playerId = UUID.randomUUID();

        var profile = coordinator.observe(
                playerId,
                new PlayerThermalState(-0.50D),
                environment(-5.0D)
        );

        assertTrue(profile.breath().enabled());
        assertTrue(profile.frostEnabled());
        assertTrue(coordinator.breathEligible(playerId));
        assertEquals(1, coordinator.observedPlayers());
        assertTrue(coordinator.pulse(
                playerId,
                Duration.ofMillis(250)
        ).isPresent());
    }

    @Test
    void ambienteQuenteDesabilitaBreathEReiniciaCadencia() {
        ThermalFeedbackCoordinator coordinator = new ThermalFeedbackCoordinator();
        UUID playerId = UUID.randomUUID();

        coordinator.observe(
                playerId,
                new PlayerThermalState(-0.50D),
                environment(-5.0D)
        );
        var warm = coordinator.observe(
                playerId,
                new PlayerThermalState(-0.50D),
                environment(20.0D)
        );

        assertFalse(warm.breath().enabled());
        assertFalse(coordinator.breathEligible(playerId));
        assertFalse(coordinator.pulse(
                playerId,
                Duration.ofSeconds(10)
        ).orElseThrow().emitBreath());
    }

    @Test
    void resetRemoveProfileECountdown() {
        ThermalFeedbackCoordinator coordinator = new ThermalFeedbackCoordinator();
        UUID playerId = UUID.randomUUID();
        coordinator.observe(
                playerId,
                PlayerThermalState.neutral(),
                environment(-5.0D)
        );

        coordinator.reset(playerId);

        assertTrue(coordinator.profile(playerId).isEmpty());
        assertTrue(coordinator.pulse(
                playerId,
                Duration.ofSeconds(1)
        ).isEmpty());
    }

    @Test
    void jogadorSemBreathNemFrostNaoFicaObservado() {
        ThermalFeedbackCoordinator coordinator = new ThermalFeedbackCoordinator();
        UUID playerId = UUID.randomUUID();

        var profile = coordinator.observe(
                playerId,
                PlayerThermalState.neutral(),
                environment(20.0D)
        );

        assertFalse(profile.breath().enabled());
        assertFalse(profile.frostEnabled());
        assertEquals(0, coordinator.observedPlayers());
        assertTrue(coordinator.pulse(
                playerId,
                Duration.ofSeconds(1)
        ).isEmpty());
    }

    @Test
    void clearRemoveTodosOsProfilesECountdowns() {
        ThermalFeedbackCoordinator coordinator = new ThermalFeedbackCoordinator();
        coordinator.observe(
                UUID.randomUUID(),
                new PlayerThermalState(-0.50D),
                environment(-5.0D)
        );
        coordinator.observe(
                UUID.randomUUID(),
                new PlayerThermalState(-0.50D),
                environment(-5.0D)
        );
        assertEquals(2, coordinator.observedPlayers());

        coordinator.clear();

        assertEquals(0, coordinator.observedPlayers());
    }

    private ThermalEnvironmentContext environment(double celsius) {
        return new ThermalEnvironmentContext(
                new AmbientTemperature(celsius),
                WaterExposure.none(),
                PlayerActivity.RESTING,
                WindExposure.calm(),
                ShelterFactor.exposed(),
                ArmorThermalLoadout.empty(),
                List.of()
        );
    }
}
