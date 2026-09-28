package dev.signalshards.livingworld.features.climate.domain;

/**
 * Tuning de calor metabólico acima do baseline corporal.
 *
 * <p>Os valores são contribuições normalizadas de gameplay por segundo,
 * não watts nem estimativas fisiológicas clínicas.</p>
 */
public record ActivityThermalSettings(
        double walkingLoadPerSecond,
        double sprintingLoadPerSecond,
        double swimmingLoadPerSecond,
        double climbingLoadPerSecond,
        double glidingLoadPerSecond,
        double maximumLoadPerSecond
) {
    public ActivityThermalSettings {
        double[] values = {
                walkingLoadPerSecond,
                sprintingLoadPerSecond,
                swimmingLoadPerSecond,
                climbingLoadPerSecond,
                glidingLoadPerSecond,
                maximumLoadPerSecond
        };
        for (double value : values) {
            if (!Double.isFinite(value) || value < 0.0D) {
                throw new IllegalArgumentException(
                        "As taxas metabólicas devem ser finitas e não negativas"
                );
            }
        }
        if (maximumLoadPerSecond <= 0.0D) {
            throw new IllegalArgumentException("O limite metabólico deve ser positivo");
        }
        if (walkingLoadPerSecond > maximumLoadPerSecond
                || sprintingLoadPerSecond > maximumLoadPerSecond
                || swimmingLoadPerSecond > maximumLoadPerSecond
                || climbingLoadPerSecond > maximumLoadPerSecond
                || glidingLoadPerSecond > maximumLoadPerSecond) {
            throw new IllegalArgumentException(
                    "Nenhuma atividade pode exceder o limite metabólico configurado"
            );
        }
    }

    public static ActivityThermalSettings livingWorldDefaults() {
        return new ActivityThermalSettings(
                0.0015D,
                0.0040D,
                0.0050D,
                0.0030D,
                0.0005D,
                0.0060D
        );
    }
}
