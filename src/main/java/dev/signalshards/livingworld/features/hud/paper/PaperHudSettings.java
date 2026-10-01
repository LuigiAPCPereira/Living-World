package dev.signalshards.livingworld.features.hud.paper;

public record PaperHudSettings(
        boolean enabled,
        long updatePeriodTicks,
        boolean calendarBossBarEnabled,
        boolean navigationEnabled,
        boolean coordinatesEnabled,
        boolean temperatureEnabled,
        boolean thermalContextEnabled,
        double wetnessMinimumDisplay
) {
    public PaperHudSettings {
        if (updatePeriodTicks < 5L || updatePeriodTicks > 100L) {
            throw new IllegalArgumentException(
                    "O período do HUD deve ficar entre 5 e 100 ticks"
            );
        }
        if (!Double.isFinite(wetnessMinimumDisplay)
                || wetnessMinimumDisplay < 0.0D
                || wetnessMinimumDisplay > 1.0D) {
            throw new IllegalArgumentException(
                    "O limiar de wetness do HUD deve ficar entre 0 e 1"
            );
        }
    }

    public static PaperHudSettings defaults() {
        return new PaperHudSettings(
                true,
                10L,
                true,
                true,
                true,
                true,
                true,
                0.15D
        );
    }

    public boolean actionBarEnabled() {
        return navigationEnabled || coordinatesEnabled || temperatureEnabled;
    }
}
