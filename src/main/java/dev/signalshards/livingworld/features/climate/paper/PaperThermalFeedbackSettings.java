package dev.signalshards.livingworld.features.climate.paper;

public record PaperThermalFeedbackSettings(
        boolean enabled,
        long updatePeriodTicks,
        boolean coldBreathEnabled,
        boolean frostEnabled
) {
    public PaperThermalFeedbackSettings {
        if (updatePeriodTicks <= 0L) {
            throw new IllegalArgumentException(
                    "O período do feedback térmico deve ser positivo"
            );
        }
    }

    public PaperThermalFeedbackSettings(boolean enabled, long updatePeriodTicks) {
        this(enabled, updatePeriodTicks, true, true);
    }

    public static PaperThermalFeedbackSettings defaults() {
        return new PaperThermalFeedbackSettings(true, 10L, true, true);
    }
}
