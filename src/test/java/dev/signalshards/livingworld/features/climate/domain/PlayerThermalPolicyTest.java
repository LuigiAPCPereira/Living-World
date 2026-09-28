package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerThermalPolicyTest {
    private final PlayerThermalPolicy policy = new PlayerThermalPolicy();

    @Test
    void estadoNeutroECategoricamenteConfortavel() {
        assertEquals(
                PlayerThermalBand.COMFORTABLE,
                policy.bandFor(PlayerThermalState.neutral())
        );
    }

    @Test
    void classificaTodasAsFaixasComDefaults() {
        assertEquals(PlayerThermalBand.EXTREME_COLD, policy.bandFor(new PlayerThermalState(-0.90D)));
        assertEquals(PlayerThermalBand.FREEZING, policy.bandFor(new PlayerThermalState(-0.55D)));
        assertEquals(PlayerThermalBand.VERY_COLD, policy.bandFor(new PlayerThermalState(-0.35D)));
        assertEquals(PlayerThermalBand.COLD, policy.bandFor(new PlayerThermalState(-0.25D)));
        assertEquals(PlayerThermalBand.COOL, policy.bandFor(new PlayerThermalState(-0.15D)));
        assertEquals(PlayerThermalBand.COMFORTABLE, policy.bandFor(new PlayerThermalState(0.0D)));
        assertEquals(PlayerThermalBand.WARM, policy.bandFor(new PlayerThermalState(0.20D)));
        assertEquals(PlayerThermalBand.HOT, policy.bandFor(new PlayerThermalState(0.35D)));
        assertEquals(PlayerThermalBand.OVERHEATING, policy.bandFor(new PlayerThermalState(0.55D)));
        assertEquals(PlayerThermalBand.EXTREME_HEAT, policy.bandFor(new PlayerThermalState(0.90D)));
    }

    @Test
    void exposicaoAcumulaAoLongoDoTempo() {
        PlayerThermalState state = policy.advance(
                PlayerThermalState.neutral(),
                new ThermalExchangeRate(-0.025D),
                Duration.ofSeconds(10)
        );

        assertEquals(-0.25D, state.thermalLoad(), 1.0E-9D);
        assertEquals(PlayerThermalBand.COLD, policy.bandFor(state));
    }

    @Test
    void integracaoFracionadaProduzOMesmoResultado() {
        ThermalExchangeRate cooling = new ThermalExchangeRate(-0.025D);
        PlayerThermalState oneStep = policy.advance(
                PlayerThermalState.neutral(),
                cooling,
                Duration.ofSeconds(10)
        );

        PlayerThermalState manySteps = PlayerThermalState.neutral();
        for (int index = 0; index < 10; index++) {
            manySteps = policy.advance(manySteps, cooling, Duration.ofSeconds(1));
        }

        assertEquals(oneStep.thermalLoad(), manySteps.thermalLoad(), 1.0E-9D);
        assertEquals(policy.bandFor(oneStep), policy.bandFor(manySteps));
    }

    @Test
    void exposicaoExtremaSaturaSemEstourarOEstado() {
        PlayerThermalState cold = policy.advance(
                PlayerThermalState.neutral(),
                new ThermalExchangeRate(-10.0D),
                Duration.ofSeconds(10)
        );
        PlayerThermalState hot = policy.advance(
                PlayerThermalState.neutral(),
                new ThermalExchangeRate(10.0D),
                Duration.ofSeconds(10)
        );

        assertEquals(PlayerThermalState.MIN_LOAD, cold.thermalLoad());
        assertEquals(PlayerThermalBand.EXTREME_COLD, policy.bandFor(cold));
        assertEquals(PlayerThermalState.MAX_LOAD, hot.thermalLoad());
        assertEquals(PlayerThermalBand.EXTREME_HEAT, policy.bandFor(hot));
    }

    @Test
    void tempoZeroMantemEstadoETempoNegativoFalha() {
        PlayerThermalState initial = new PlayerThermalState(0.42D);
        assertEquals(
                initial,
                policy.advance(initial, new ThermalExchangeRate(-1.0D), Duration.ZERO)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.advance(
                        initial,
                        ThermalExchangeRate.neutral(),
                        Duration.ofMillis(-1)
                )
        );
    }
}
