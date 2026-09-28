package dev.signalshards.livingworld.features.climate.domain;

/**
 * Fração normalizada de precipitação que realmente alcança o jogador.
 */
public record PrecipitationExposure(double level) {
    public PrecipitationExposure {
        if (!Double.isFinite(level) || level < 0.0D || level > 1.0D) {
            throw new IllegalArgumentException(
                    "A exposição à precipitação deve ficar entre 0 e 1"
            );
        }
    }

    public static PrecipitationExposure none() {
        return new PrecipitationExposure(0.0D);
    }
}
