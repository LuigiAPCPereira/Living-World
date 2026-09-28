package dev.signalshards.livingworld.features.ecology.domain;

public record PhysicalWinterSettings(
        double freezeAtOrBelowCelsius,
        double thawAtOrAboveCelsius,
        int maxOwnedPositionsPerChunk
) {
    public PhysicalWinterSettings {
        if (!Double.isFinite(freezeAtOrBelowCelsius)
                || !Double.isFinite(thawAtOrAboveCelsius)
                || freezeAtOrBelowCelsius >= thawAtOrAboveCelsius) {
            throw new IllegalArgumentException(
                    "Freeze deve ser menor que thaw para existir histerese"
            );
        }
        if (maxOwnedPositionsPerChunk <= 0) {
            throw new IllegalArgumentException(
                    "O limite de ownership por chunk deve ser positivo"
            );
        }
    }

    public static PhysicalWinterSettings defaults() {
        return new PhysicalWinterSettings(
                -1.0D,
                2.0D,
                256
        );
    }
}
