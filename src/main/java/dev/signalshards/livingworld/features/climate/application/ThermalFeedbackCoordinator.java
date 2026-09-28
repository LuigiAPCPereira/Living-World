package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.PlayerThermalState;
import dev.signalshards.livingworld.features.climate.domain.ThermalFeedbackPolicy;
import dev.signalshards.livingworld.features.climate.domain.ThermalFeedbackProfile;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Ponte application-level entre o runtime térmico coarse e o pulse visual barato.
 *
 * <p>O ambiente é observado apenas pelo runtime térmico. Pulses visuais usam o
 * último profile calculado e não repetem probes Paper.</p>
 */
public final class ThermalFeedbackCoordinator {
    private final ThermalFeedbackPolicy policy;
    private final ThermalFeedbackRuntimeService runtime;
    private final Map<UUID, ThermalFeedbackProfile> profiles = new HashMap<>();

    public ThermalFeedbackCoordinator() {
        this(
                new ThermalFeedbackPolicy(),
                new ThermalFeedbackRuntimeService()
        );
    }

    ThermalFeedbackCoordinator(
            ThermalFeedbackPolicy policy,
            ThermalFeedbackRuntimeService runtime
    ) {
        this.policy = Objects.requireNonNull(policy, "policy de feedback");
        this.runtime = Objects.requireNonNull(runtime, "runtime de feedback");
    }

    public ThermalFeedbackProfile observe(
            UUID playerId,
            PlayerThermalState thermalState,
            ThermalEnvironmentContext environment
    ) {
        Objects.requireNonNull(playerId, "id do jogador");
        Objects.requireNonNull(thermalState, "estado térmico");
        Objects.requireNonNull(environment, "contexto ambiental");

        ThermalFeedbackProfile profile = policy.profileFor(
                thermalState,
                environment.ambientTemperature(),
                environment.activity(),
                environment.waterExposure()
        );
        if (!profile.breath().enabled() && !profile.frostEnabled()) {
            profiles.remove(playerId);
            runtime.reset(playerId);
            return profile;
        }
        profiles.put(playerId, profile);
        runtime.advance(playerId, profile, Duration.ZERO);
        return profile;
    }

    public Optional<ThermalFeedbackDecision> pulse(
            UUID playerId,
            Duration elapsed
    ) {
        Objects.requireNonNull(playerId, "id do jogador");
        ThermalFeedbackProfile profile = profiles.get(playerId);
        if (profile == null) {
            return Optional.empty();
        }
        return Optional.of(runtime.advance(playerId, profile, elapsed));
    }

    public Optional<ThermalFeedbackProfile> profile(UUID playerId) {
        return Optional.ofNullable(profiles.get(
                Objects.requireNonNull(playerId, "id do jogador")
        ));
    }

    public boolean breathEligible(UUID playerId) {
        return profile(playerId)
                .map(profile -> profile.breath().enabled())
                .orElse(false);
    }

    public boolean feedbackEligible(UUID playerId) {
        return profile(playerId).isPresent();
    }

    public void reset(UUID playerId) {
        Objects.requireNonNull(playerId, "id do jogador");
        profiles.remove(playerId);
        runtime.reset(playerId);
    }

    public void clear() {
        profiles.clear();
        runtime.clear();
    }

    public int observedPlayers() {
        return profiles.size();
    }
}
