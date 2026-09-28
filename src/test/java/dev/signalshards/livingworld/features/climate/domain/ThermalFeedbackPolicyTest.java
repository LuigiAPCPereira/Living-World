package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThermalFeedbackPolicyTest {
    private final ThermalFeedbackPolicy policy = new ThermalFeedbackPolicy();

    @Test
    void arQuenteNaoMostraRespiracaoMesmoComCorpoMuitoFrio() {
        ThermalFeedbackProfile profile = policy.profileFor(
                new PlayerThermalState(-0.90D),
                new AmbientTemperature(15.0D),
                PlayerActivity.RESTING,
                WaterExposure.none()
        );

        assertFalse(profile.breath().enabled());
        assertTrue(profile.frostEnabled());
    }

    @Test
    void arMaisFrioAumentaRespiracaoEEncurtaCadencia() {
        BreathFeedback mild = policy.profileFor(
                PlayerThermalState.neutral(),
                new AmbientTemperature(5.0D),
                PlayerActivity.RESTING,
                WaterExposure.none()
        ).breath();
        BreathFeedback severe = policy.profileFor(
                PlayerThermalState.neutral(),
                new AmbientTemperature(-15.0D),
                PlayerActivity.RESTING,
                WaterExposure.none()
        ).breath();

        assertTrue(mild.enabled());
        assertTrue(severe.intensity() > mild.intensity());
        assertTrue(
                severe.minimumInterval().compareTo(mild.minimumInterval()) < 0
        );
        assertTrue(
                severe.maximumInterval().compareTo(mild.maximumInterval()) < 0
        );
    }

    @Test
    void sprintAumentaFrequenciaSemMudarIntensidadeDoVapor() {
        BreathFeedback resting = policy.profileFor(
                PlayerThermalState.neutral(),
                new AmbientTemperature(0.0D),
                PlayerActivity.RESTING,
                WaterExposure.none()
        ).breath();
        BreathFeedback sprinting = policy.profileFor(
                PlayerThermalState.neutral(),
                new AmbientTemperature(0.0D),
                PlayerActivity.SPRINTING,
                WaterExposure.none()
        ).breath();

        assertTrue(resting.intensity() == sprinting.intensity());
        assertTrue(
                sprinting.minimumInterval().compareTo(resting.minimumInterval()) < 0
        );
    }

    @Test
    void submersaoTotalSuprimeVaporNaBoca() {
        ThermalFeedbackProfile profile = policy.profileFor(
                PlayerThermalState.neutral(),
                new AmbientTemperature(-10.0D),
                PlayerActivity.SWIMMING,
                new WaterExposure(1.0D, 4.0D)
        );

        assertFalse(profile.breath().enabled());
    }

    @Test
    void frostEhProgressivoEIndependenteDoVapor() {
        ThermalFeedbackProfile comfortable = policy.profileFor(
                PlayerThermalState.neutral(),
                new AmbientTemperature(20.0D),
                PlayerActivity.RESTING,
                WaterExposure.none()
        );
        ThermalFeedbackProfile cold = policy.profileFor(
                new PlayerThermalState(-0.50D),
                new AmbientTemperature(20.0D),
                PlayerActivity.RESTING,
                WaterExposure.none()
        );
        ThermalFeedbackProfile extreme = policy.profileFor(
                new PlayerThermalState(-0.90D),
                new AmbientTemperature(20.0D),
                PlayerActivity.RESTING,
                WaterExposure.none()
        );

        assertFalse(comfortable.frostEnabled());
        assertTrue(cold.frostIntensity() > 0.0D);
        assertTrue(extreme.frostIntensity() > cold.frostIntensity());
        assertFalse(extreme.breath().enabled());
    }
}
