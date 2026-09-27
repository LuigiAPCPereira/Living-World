package dev.signalshards.livingworld.features.waystones.paper;

import org.bukkit.Material;

import java.util.Objects;

public record PaperWaystoneSettings(
        boolean enabled,
        Material anchorMaterial,
        double renameMaxDistance
) {
    public PaperWaystoneSettings {
        Objects.requireNonNull(anchorMaterial, "material da âncora");
        if (!Double.isFinite(renameMaxDistance)
                || renameMaxDistance < 1.0D
                || renameMaxDistance > 16.0D) {
            throw new IllegalArgumentException(
                    "A distância máxima para renomear waystones deve ficar entre 1 e 16 blocos"
            );
        }
    }

    public static PaperWaystoneSettings defaults() {
        return new PaperWaystoneSettings(true, Material.LODESTONE, 6.0D);
    }
}
