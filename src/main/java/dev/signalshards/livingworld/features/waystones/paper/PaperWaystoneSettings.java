package dev.signalshards.livingworld.features.waystones.paper;

import org.bukkit.Material;

import java.util.Objects;

public record PaperWaystoneSettings(boolean enabled, Material anchorMaterial) {
    public PaperWaystoneSettings {
        Objects.requireNonNull(anchorMaterial, "material da âncora");
    }

    public static PaperWaystoneSettings defaults() {
        return new PaperWaystoneSettings(true, Material.LODESTONE);
    }
}
