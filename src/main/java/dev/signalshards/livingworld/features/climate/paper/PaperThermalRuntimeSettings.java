package dev.signalshards.livingworld.features.climate.paper;

public record PaperThermalRuntimeSettings(
        boolean enabled,
        long updatePeriodTicks
) {
    public PaperThermalRuntimeSettings {
        if (updatePeriodTicks <= 0L) {
            throw new IllegalArgumentException(
                    "O período do runtime térmico deve ser positivo"
            );
        }
    }

    public static PaperThermalRuntimeSettings defaults() {
        return new PaperThermalRuntimeSettings(true, 20L);
    }
}
