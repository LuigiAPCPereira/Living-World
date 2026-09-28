package dev.signalshards.livingworld.features.climate.domain;

/**
 * Proteção normalizada fornecida pelo ambiente construído/natural.
 *
 * <p>0 significa exposto e 1 significa fortemente abrigado. O valor não
 * implica que Living World reconheça semanticamente uma casa.</p>
 */
public record ShelterFactor(double level) {
    public ShelterFactor {
        if (!Double.isFinite(level) || level < 0.0D || level > 1.0D) {
            throw new IllegalArgumentException(
                    "O fator de abrigo deve ser finito e ficar entre 0 e 1"
            );
        }
    }

    public static ShelterFactor exposed() {
        return new ShelterFactor(0.0D);
    }
}
