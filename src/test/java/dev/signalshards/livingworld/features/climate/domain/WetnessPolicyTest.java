package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WetnessPolicyTest {
    private final WetnessPolicy policy = new WetnessPolicy();

    @Test
    void molhamentoAcumulaAoLongoDoTempo() {
        WetnessState state = policy.advance(
                WetnessState.dry(),
                new WetnessRate(0.08D),
                Duration.ofSeconds(5)
        );

        assertEquals(0.40D, state.level(), 1.0E-9D);
    }

    @Test
    void secagemReduzEstadoSemPassarDeSeco() {
        WetnessState state = policy.advance(
                new WetnessState(0.30D),
                new WetnessRate(-0.20D),
                Duration.ofSeconds(5)
        );

        assertEquals(WetnessState.DRY, state.level());
    }

    @Test
    void passosFracionadosSaoEquivalentes() {
        WetnessRate rain = new WetnessRate(0.03D);
        WetnessState oneStep = policy.advance(
                WetnessState.dry(),
                rain,
                Duration.ofSeconds(10)
        );

        WetnessState manySteps = WetnessState.dry();
        for (int index = 0; index < 10; index++) {
            manySteps = policy.advance(manySteps, rain, Duration.ofSeconds(1));
        }

        assertEquals(oneStep.level(), manySteps.level(), 1.0E-9D);
    }

    @Test
    void tempoNegativoFalha() {
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.advance(
                        WetnessState.dry(),
                        WetnessRate.neutral(),
                        Duration.ofMillis(-1)
                )
        );
    }
}
