package dev.signalshards.livingworld.features.climate.application;

public record ClimateRuleReadout(boolean enabled, int percent) {
    public ClimateRuleReadout {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException(
                    "O percentual climático deve ficar entre 0 e 100"
            );
        }
    }
}
