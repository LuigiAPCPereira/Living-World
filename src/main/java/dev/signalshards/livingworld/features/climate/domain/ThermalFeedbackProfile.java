package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Feedback corporal abstrato. Não aplica efeitos ou dano.
 */
public record ThermalFeedbackProfile(
        BreathFeedback breath,
        double frostIntensity
) {
    public ThermalFeedbackProfile {
        Objects.requireNonNull(breath, "feedback de respiração");
        if (!Double.isFinite(frostIntensity)
                || frostIntensity < 0.0D
                || frostIntensity > 1.0D) {
            throw new IllegalArgumentException(
                    "A intensidade de frost deve ficar entre 0 e 1"
            );
        }
    }

    public boolean frostEnabled() {
        return frostIntensity > 0.0D;
    }
}
