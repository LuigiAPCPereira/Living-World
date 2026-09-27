package dev.signalshards.livingworld.features.climate.domain;

public record ClimateProfileThresholds(
        double freezingMax,
        double coldMax,
        double temperateMax,
        double hotMax,
        double aridMax,
        double dryMax,
        double balancedMax,
        double humidMax
) {
    public ClimateProfileThresholds {
        requireStrictlyIncreasing(
                "temperatura",
                freezingMax,
                coldMax,
                temperateMax,
                hotMax
        );
        requireStrictlyIncreasing(
                "umidade",
                aridMax,
                dryMax,
                balancedMax,
                humidMax
        );
    }

    /**
     * Política inicial do Living World. Estes cortes são regras do plugin, não
     * constantes oficiais do Paper/Minecraft, e podem evoluir sem alterar o
     * formato persistido do calendário.
     */
    public static ClimateProfileThresholds livingWorldDefaults() {
        return new ClimateProfileThresholds(
                0.15,
                0.50,
                0.90,
                1.50,
                0.10,
                0.35,
                0.65,
                0.90
        );
    }

    private static void requireStrictlyIncreasing(String label, double... values) {
        for (double value : values) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Os limites de " + label + " devem ser finitos");
            }
        }

        for (int index = 1; index < values.length; index++) {
            if (values[index] <= values[index - 1]) {
                throw new IllegalArgumentException(
                        "Os limites de " + label + " devem estar em ordem estritamente crescente"
                );
            }
        }
    }
}
