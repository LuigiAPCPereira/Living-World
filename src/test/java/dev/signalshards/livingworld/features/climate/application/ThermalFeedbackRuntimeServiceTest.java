package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.BreathFeedback;
import dev.signalshards.livingworld.features.climate.domain.ThermalFeedbackProfile;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThermalFeedbackRuntimeServiceTest {
    private final ThermalFeedbackProfile enabled = new ThermalFeedbackProfile(
            new BreathFeedback(
                    0.5D,
                    Duration.ofSeconds(2),
                    Duration.ofSeconds(6)
            ),
            0.4D
    );
    private final ThermalFeedbackProfile disabled = new ThermalFeedbackProfile(
            BreathFeedback.none(),
            0.4D
    );

    @Test
    void primeiroPulseAgendaSemEmitirSincronizado() {
        ThermalFeedbackRuntimeService runtime = runtimeWith(0.5D);
        UUID playerId = UUID.randomUUID();

        ThermalFeedbackDecision first = runtime.advance(
                playerId,
                enabled,
                Duration.ofSeconds(10)
        );

        assertFalse(first.emitBreath());
        assertEquals(1, runtime.trackedPlayers());
    }

    @Test
    void emiteQuandoCountdownExpiraEReagenda() {
        ThermalFeedbackRuntimeService runtime = runtimeWith(0.0D, 1.0D, 0.5D);
        UUID playerId = UUID.randomUUID();

        runtime.advance(playerId, enabled, Duration.ZERO);
        assertFalse(runtime.advance(
                playerId,
                enabled,
                Duration.ofSeconds(1)
        ).emitBreath());
        assertTrue(runtime.advance(
                playerId,
                enabled,
                Duration.ofSeconds(1)
        ).emitBreath());
        assertFalse(runtime.advance(
                playerId,
                enabled,
                Duration.ofSeconds(5)
        ).emitBreath());
        assertTrue(runtime.advance(
                playerId,
                enabled,
                Duration.ofSeconds(1)
        ).emitBreath());
    }

    @Test
    void catchUpLongoNuncaEmiteBurst() {
        ThermalFeedbackRuntimeService runtime = runtimeWith(0.0D, 0.0D);
        UUID playerId = UUID.randomUUID();

        runtime.advance(playerId, enabled, Duration.ZERO);
        ThermalFeedbackDecision decision = runtime.advance(
                playerId,
                enabled,
                Duration.ofMinutes(5)
        );

        assertTrue(decision.emitBreath());
        assertEquals(1, runtime.trackedPlayers());
        assertFalse(runtime.advance(
                playerId,
                enabled,
                Duration.ofSeconds(1)
        ).emitBreath());
    }

    @Test
    void jogadoresRecebemJitterIndependente() {
        ThermalFeedbackRuntimeService runtime = runtimeWith(0.0D, 1.0D, 0.5D);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        runtime.advance(first, enabled, Duration.ZERO);
        runtime.advance(second, enabled, Duration.ZERO);

        assertTrue(runtime.advance(
                first,
                enabled,
                Duration.ofSeconds(2)
        ).emitBreath());
        assertFalse(runtime.advance(
                second,
                enabled,
                Duration.ofSeconds(2)
        ).emitBreath());
    }

    @Test
    void inelegibilidadeEResetRemovemEstado() {
        ThermalFeedbackRuntimeService runtime = runtimeWith(0.5D, 0.5D);
        UUID playerId = UUID.randomUUID();

        runtime.advance(playerId, enabled, Duration.ZERO);
        assertEquals(1, runtime.trackedPlayers());
        runtime.advance(playerId, disabled, Duration.ofSeconds(1));
        assertEquals(0, runtime.trackedPlayers());

        runtime.advance(playerId, enabled, Duration.ZERO);
        runtime.reset(playerId);
        assertEquals(0, runtime.trackedPlayers());
    }

    @Test
    void frioMaisSeveroPodeAnteciparEsperaJaAgendada() {
        ThermalFeedbackRuntimeService runtime = runtimeWith(1.0D, 0.0D);
        UUID playerId = UUID.randomUUID();
        ThermalFeedbackProfile mild = new ThermalFeedbackProfile(
                new BreathFeedback(
                        0.2D,
                        Duration.ofSeconds(5),
                        Duration.ofSeconds(8)
                ),
                0.0D
        );
        ThermalFeedbackProfile severe = new ThermalFeedbackProfile(
                new BreathFeedback(
                        0.9D,
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(2)
                ),
                0.0D
        );

        runtime.advance(playerId, mild, Duration.ZERO);
        assertTrue(runtime.advance(
                playerId,
                severe,
                Duration.ofSeconds(2)
        ).emitBreath());
    }

    @Test
    void clearRemoveTodosOsJogadoresRastreados() {
        ThermalFeedbackRuntimeService runtime = runtimeWith(0.5D, 0.5D);
        runtime.advance(UUID.randomUUID(), enabled, Duration.ZERO);
        runtime.advance(UUID.randomUUID(), enabled, Duration.ZERO);
        assertEquals(2, runtime.trackedPlayers());

        runtime.clear();

        assertEquals(0, runtime.trackedPlayers());
    }

    private ThermalFeedbackRuntimeService runtimeWith(double... values) {
        Queue<Double> samples = new ArrayDeque<>();
        for (double value : values) {
            samples.add(value);
        }
        DoubleSupplier supplier = () -> {
            Double value = samples.poll();
            if (value == null) {
                throw new AssertionError("Amostra de jitter não configurada");
            }
            return value;
        };
        return new ThermalFeedbackRuntimeService(
                new ThermalFeedbackCadencePolicy(),
                supplier
        );
    }
}
