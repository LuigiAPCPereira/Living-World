package dev.signalshards.livingworld.features.climate.domain;

/**
 * Propriedades térmicas normalizadas de um material de armadura.
 *
 * <p>Insulation reduz troca térmica com o ar. Water resistance é preservada
 * separadamente para um slice posterior e não altera wetness ainda.</p>
 */
public record ArmorMaterialThermalProfile(
        double insulation,
        double waterResistance
) {
    public ArmorMaterialThermalProfile {
        validateUnit(insulation, "isolamento");
        validateUnit(waterResistance, "resistência à água");
    }

    private static void validateUnit(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D || value > 1.0D) {
            throw new IllegalArgumentException(name + " deve ser finito e ficar entre 0 e 1");
        }
    }
}
