package dev.signalshards.livingworld.features.climate.domain;

/**
 * Observação já resolvida de uma fonte térmica próxima.
 *
 * <p>Distância é geométrica; exposure e line-of-sight ficam em [0,1].
 * O domínio não procura blocos no mundo.</p>
 */
public record LocalHeatExposure(
        LocalHeatSourceType sourceType,
        double distanceBlocks,
        double exposureFactor,
        double lineOfSightFactor
) {
    public LocalHeatExposure {
        if (sourceType == null) {
            throw new NullPointerException("tipo de fonte térmica");
        }
        if (!Double.isFinite(distanceBlocks) || distanceBlocks < 0.0D) {
            throw new IllegalArgumentException("A distância deve ser finita e não negativa");
        }
        validateUnit(exposureFactor, "fator de exposição");
        validateUnit(lineOfSightFactor, "fator de linha de visão");
    }

    private static void validateUnit(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D || value > 1.0D) {
            throw new IllegalArgumentException(name + " deve ficar entre 0 e 1");
        }
    }
}
