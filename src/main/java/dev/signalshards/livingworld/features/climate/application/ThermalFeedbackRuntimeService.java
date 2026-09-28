package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.ThermalFeedbackProfile;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

/**
 * Estado runtime mínimo para cadence/jitter do feedback térmico.
 *
 * <p>Uma chamada pode emitir no máximo um breath por jogador, mesmo depois de
 * uma pausa longa. Assim lag/catch-up nunca vira burst de partículas.</p>
 */
public final class ThermalFeedbackRuntimeService {
    private final ThermalFeedbackCadencePolicy cadencePolicy;
    private final DoubleSupplier randomUnit;
    private final Map<UUID, Duration> breathCountdowns = new HashMap<>();

    public ThermalFeedbackRuntimeService() {
        this(
                new ThermalFeedbackCadencePolicy(),
                () -> ThreadLocalRandom.current().nextDouble()
        );
    }

    ThermalFeedbackRuntimeService(
            ThermalFeedbackCadencePolicy cadencePolicy,
            DoubleSupplier randomUnit
    ) {
        this.cadencePolicy = Objects.requireNonNull(
                cadencePolicy,
                "policy de cadência"
        );
        this.randomUnit = Objects.requireNonNull(
                randomUnit,
                "fonte de jitter"
        );
    }

    public ThermalFeedbackDecision advance(
            UUID playerId,
            ThermalFeedbackProfile profile,
            Duration elapsed
    ) {
        Objects.requireNonNull(playerId, "id do jogador");
        Objects.requireNonNull(profile, "perfil de feedback");
        Objects.requireNonNull(elapsed, "tempo decorrido");
        if (elapsed.isNegative()) {
            throw new IllegalArgumentException(
                    "O tempo decorrido não pode ser negativo"
            );
        }

        if (!profile.breath().enabled()) {
            breathCountdowns.remove(playerId);
            return new ThermalFeedbackDecision(profile, false);
        }

        Duration remaining = breathCountdowns.get(playerId);
        if (remaining == null) {
            breathCountdowns.put(playerId, nextDelay(profile));
            return new ThermalFeedbackDecision(profile, false);
        }

        Duration boundedRemaining = minimum(
                remaining,
                profile.breath().maximumInterval()
        );
        Duration next = boundedRemaining.minus(elapsed);
        if (!next.isNegative() && !next.isZero()) {
            breathCountdowns.put(playerId, next);
            return new ThermalFeedbackDecision(profile, false);
        }

        breathCountdowns.put(playerId, nextDelay(profile));
        return new ThermalFeedbackDecision(profile, true);
    }

    public void reset(UUID playerId) {
        breathCountdowns.remove(Objects.requireNonNull(playerId, "id do jogador"));
    }

    public void clear() {
        breathCountdowns.clear();
    }

    public int trackedPlayers() {
        return breathCountdowns.size();
    }

    private Duration nextDelay(ThermalFeedbackProfile profile) {
        return cadencePolicy.nextDelay(
                profile.breath(),
                randomUnit.getAsDouble()
        );
    }

    private Duration minimum(Duration left, Duration right) {
        return left.compareTo(right) <= 0 ? left : right;
    }
}
