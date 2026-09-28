package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirTransferPolicyTest {
    private final AirTransferPolicy policy = new AirTransferPolicy();

    @Test
    void calmoEExpostoMantemBaseline() {
        assertEquals(
                1.0D,
                policy.factorFor(WindExposure.calm(), ShelterFactor.exposed()).value(),
                1.0E-9D
        );
    }

    @Test
    void ventoAumentaTransferencia() {
        double calm = policy.factorFor(
                WindExposure.calm(),
                ShelterFactor.exposed()
        ).value();
        double windy = policy.factorFor(
                new WindExposure(1.0D),
                ShelterFactor.exposed()
        ).value();

        assertTrue(windy > calm);
        assertEquals(2.0D, windy, 1.0E-9D);
    }

    @Test
    void abrigoReduzTransferencia() {
        double exposed = policy.factorFor(
                WindExposure.calm(),
                ShelterFactor.exposed()
        ).value();
        double sheltered = policy.factorFor(
                WindExposure.calm(),
                new ShelterFactor(1.0D)
        ).value();

        assertTrue(sheltered < exposed);
        assertEquals(0.25D, sheltered, 1.0E-9D);
    }

    @Test
    void resultadoPermaneceBounded() {
        AirTransferPolicy bounded = new AirTransferPolicy(
                new AirTransferSettings(100.0D, 1.0D, 0.20D, 2.50D)
        );

        assertEquals(
                2.50D,
                bounded.factorFor(new WindExposure(1.0D), ShelterFactor.exposed()).value(),
                1.0E-9D
        );
        assertEquals(
                0.20D,
                bounded.factorFor(WindExposure.calm(), new ShelterFactor(1.0D)).value(),
                1.0E-9D
        );
    }
}
