package dev.signalshards.livingworld.features.climate.domain;

public enum MoistureBand {
    ARIDO,
    SECO,
    EQUILIBRADO,
    UMIDO,
    ENCHARCADO;

    public MoistureBand shift(int amount) {
        int target = Math.max(0, Math.min(values().length - 1, ordinal() + amount));
        return values()[target];
    }
}
