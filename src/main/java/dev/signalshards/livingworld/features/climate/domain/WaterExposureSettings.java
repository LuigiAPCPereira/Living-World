package dev.signalshards.livingworld.features.climate.domain;

/**
 * Tuning de gameplay para exposição à água.
 *
 * <p>Os defaults são sementes de balanceamento Vanilla+, não constantes
 * físicas. A direção arquitetural importa mais que os valores iniciais.</p>
 */
public record WaterExposureSettings(
        double fullSubmersionWetnessPerSecond,
        double depthForMaxTransferBonusBlocks,
        double maxDepthTransferBonus
) {
    public WaterExposureSettings {
        if (!Double.isFinite(fullSubmersionWetnessPerSecond)
                || fullSubmersionWetnessPerSecond <= 0.0D) {
            throw new IllegalArgumentException(
                    "A taxa de molhamento por submersão total deve ser positiva"
            );
        }
        if (!Double.isFinite(depthForMaxTransferBonusBlocks)
                || depthForMaxTransferBonusBlocks <= 0.0D) {
            throw new IllegalArgumentException(
                    "A profundidade de referência deve ser positiva"
            );
        }
        if (!Double.isFinite(maxDepthTransferBonus) || maxDepthTransferBonus < 0.0D) {
            throw new IllegalArgumentException(
                    "O bônus máximo de transferência por profundidade não pode ser negativo"
            );
        }
    }

    public static WaterExposureSettings livingWorldDefaults() {
        return new WaterExposureSettings(
                0.12D,
                32.0D,
                0.35D
        );
    }
}
