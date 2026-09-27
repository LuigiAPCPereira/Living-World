package dev.signalshards.livingworld.features.hud.paper;

public record PaperHudSettings(
        boolean enabled,
        long updatePeriodTicks,
        boolean calendarBossBarEnabled,
        boolean navigationEnabled,
        boolean coordinatesEnabled,
        boolean temperatureEnabled
) {
    public PaperHudSettings {
        if (updatePeriodTicks < 5L || updatePeriodTicks > 100L) {
            throw new IllegalArgumentException(
                    "O período do HUD deve ficar entre 5 e 100 ticks"
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
                true
        );
    }

    public boolean actionBarEnabled() {
        return navigationEnabled || coordinatesEnabled || temperatureEnabled;
    }
}
