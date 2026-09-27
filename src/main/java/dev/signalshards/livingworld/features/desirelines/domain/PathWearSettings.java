package dev.signalshards.livingworld.features.desirelines.domain;

public record PathWearSettings(
        boolean enabled,
        int grassToDirtVisits,
        int dirtToPathVisits,
        int maxTrackedPositionsPerChunk,
        long flushPeriodTicks
) {
    public PathWearSettings {
        if (grassToDirtVisits <= 0) {
            throw new IllegalArgumentException("O limite de grama para terra deve ser positivo");
        }
        if (dirtToPathVisits <= grassToDirtVisits) {
            throw new IllegalArgumentException(
                    "O limite de terra para caminho deve ser maior que o limite de grama"
            );
        }
        if (maxTrackedPositionsPerChunk <= 0) {
            throw new IllegalArgumentException("O limite de posições por chunk deve ser positivo");
        }
        if (flushPeriodTicks <= 0) {
            throw new IllegalArgumentException("O período de persistência deve ser positivo");
        }
    }

    public static PathWearSettings defaults() {
        return new PathWearSettings(true, 12, 24, 512, 200L);
    }
}
