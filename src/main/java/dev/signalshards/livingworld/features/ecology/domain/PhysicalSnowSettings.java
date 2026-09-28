package dev.signalshards.livingworld.features.ecology.domain;

public record PhysicalSnowSettings(int maxLayers) {
    public PhysicalSnowSettings {
        if (maxLayers < 1 || maxLayers > 8) {
            throw new IllegalArgumentException(
                    "O máximo de camadas de neve deve ficar entre 1 e 8"
            );
        }
    }

    public static PhysicalSnowSettings defaults() {
        return new PhysicalSnowSettings(4);
    }
}
