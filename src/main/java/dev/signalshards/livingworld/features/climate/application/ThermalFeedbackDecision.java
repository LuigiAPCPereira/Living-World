package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.ThermalFeedbackProfile;

import java.util.Objects;

/**
 * Resultado bounded de um pulse de feedback.
 */
public record ThermalFeedbackDecision(
        ThermalFeedbackProfile profile,
        boolean emitBreath
) {
    public ThermalFeedbackDecision {
        Objects.requireNonNull(profile, "perfil de feedback térmico");
        if (emitBreath && !profile.breath().enabled()) {
            throw new IllegalArgumentException(
                    "Não é possível emitir breath com perfil desabilitado"
            );
        }
    }
}
