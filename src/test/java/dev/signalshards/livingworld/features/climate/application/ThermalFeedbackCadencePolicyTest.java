package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.BreathFeedback;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ThermalFeedbackCadencePolicyTest {
    private final ThermalFeedbackCadencePolicy policy =
            new ThermalFeedbackCadencePolicy();
    private final BreathFeedback breath = new BreathFeedback(
            0.5D,
            Duration.ofSeconds(2),
            Duration.ofSeconds(6)
    );

    @Test
    void interpolaJitterDentroDaFaixa() {
        assertEquals(
                Duration.ofSeconds(2),
                policy.nextDelay(breath, 0.0D)
        );
        assertEquals(
                Duration.ofSeconds(4),
                policy.nextDelay(breath, 0.5D)
        );
        assertEquals(
                Duration.ofSeconds(6),
                policy.nextDelay(breath, 1.0D)
        );
    }

    @Test
    void breathDesabilitadoNaoAgenda() {
        assertEquals(
                Duration.ZERO,
                policy.nextDelay(BreathFeedback.none(), 0.5D)
        );
    }

    @Test
    void rejeitaAmostraForaDaFaixa() {
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.nextDelay(breath, -0.01D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.nextDelay(breath, 1.01D)
        );
    }
}
