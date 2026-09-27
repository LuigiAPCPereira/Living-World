package dev.signalshards.livingworld.features.waystones.paper;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public final class PaperWaystoneSettingsLoader {
    private PaperWaystoneSettingsLoader() {
    }

    public static PaperWaystoneSettings load(ConfigurationSection config) {
        return load(
                config,
                Material::matchMaterial,
                material -> material.isBlock() && !material.isAir()
        );
    }

    static PaperWaystoneSettings load(
            ConfigurationSection config,
            Function<String, Material> materialResolver,
            Predicate<Material> validAnchor
    ) {
        Objects.requireNonNull(config, "configuração");
        Objects.requireNonNull(materialResolver, "resolvedor de material");
        Objects.requireNonNull(validAnchor, "validador de âncora");
        PaperWaystoneSettings defaults = PaperWaystoneSettings.defaults();
        String configuredMaterial = config.getString(
                "waystones.anchor-material",
                defaults.anchorMaterial().name()
        );
        Material material = materialResolver.apply(configuredMaterial);
        if (material == null) {
            throw new IllegalArgumentException(
                    "Material de waystone desconhecido: " + configuredMaterial
            );
        }
        if (!validAnchor.test(material)) {
            throw new IllegalArgumentException(
                    "O material da waystone precisa ser um bloco sólido: " + material
            );
        }

        return new PaperWaystoneSettings(
                config.getBoolean("waystones.enabled", defaults.enabled()),
                material,
                config.getDouble(
                        "waystones.rename-max-distance",
                        defaults.renameMaxDistance()
                )
        );
    }
}
