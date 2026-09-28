package dev.signalshards.livingworld.features.climate.domain;

/**
 * Exposição normalizada ao vento, de 0 (sem vento relevante) a 1 (exposição máxima).
 *
 * <p>Weather, altitude, céu aberto e velocidade poderão contribuir para esse
 * valor em adapters futuros. O domínio não conhece Paper.</p>
 */
public record WindExposure(double level) {
    public WindExposure {
        if (!Double.isFinite(level) || level < 0.0D || level > 1.0D) {
            throw new IllegalArgumentException(
                    "A exposição ao vento deve ser finita e ficar entre 0 e 1"
            );
        }
    }

    public static WindExposure calm() {
        return new WindExposure(0.0D);
    }
}
