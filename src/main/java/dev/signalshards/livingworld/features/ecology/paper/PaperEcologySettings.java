package dev.signalshards.livingworld.features.ecology.paper;

public record PaperEcologySettings(
        boolean naturalGrowthEnabled,
        double cropGrowthStrength,
        double treeGrowthStrength,
        boolean groundCoverSpreadEnabled,
        double groundCoverSpreadStrength,
        boolean farmlandMoistureRetentionEnabled,
        double farmlandMoistureRetentionStrength,
        boolean frozenSurfacesEnabled
) {
    public PaperEcologySettings {
        requireStrength("plantações", cropGrowthStrength);
        requireStrength("árvores", treeGrowthStrength);
        requireStrength("cobertura vegetal", groundCoverSpreadStrength);
        requireStrength(
                "retenção de umidade do solo",
                farmlandMoistureRetentionStrength
        );
    }

    public static PaperEcologySettings defaults() {
        return new PaperEcologySettings(
                true,
                0.65D,
                0.35D,
                true,
                0.50D,
                true,
                0.70D,
                true
        );
    }

    private static void requireStrength(String target, double strength) {
        if (!Double.isFinite(strength) || strength < 0.0D || strength > 1.0D) {
            throw new IllegalArgumentException(
                    "A força ecológica de " + target + " deve ficar entre 0 e 1"
            );
        }
    }
}
