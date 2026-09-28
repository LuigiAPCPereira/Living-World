package dev.signalshards.livingworld.features.climate.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Catálogo configurável de perfis térmicos por material.
 *
 * <p>Os defaults expressam identidade Vanilla+ inicial e são tuning de
 * gameplay, não propriedades físicas reais dos materiais.</p>
 */
public final class ArmorThermalCatalog {
    private final Map<ArmorThermalMaterial, ArmorMaterialThermalProfile> profiles;

    public ArmorThermalCatalog(
            Map<ArmorThermalMaterial, ArmorMaterialThermalProfile> profiles
    ) {
        Objects.requireNonNull(profiles, "perfis térmicos");
        EnumMap<ArmorThermalMaterial, ArmorMaterialThermalProfile> copy =
                new EnumMap<>(ArmorThermalMaterial.class);
        copy.putAll(profiles);
        for (ArmorThermalMaterial material : ArmorThermalMaterial.values()) {
            if (!copy.containsKey(material)) {
                throw new IllegalArgumentException("Perfil ausente para " + material);
            }
            Objects.requireNonNull(copy.get(material), "perfil de " + material);
        }
        this.profiles = Map.copyOf(copy);
    }

    public static ArmorThermalCatalog livingWorldDefaults() {
        EnumMap<ArmorThermalMaterial, ArmorMaterialThermalProfile> profiles =
                new EnumMap<>(ArmorThermalMaterial.class);
        profiles.put(ArmorThermalMaterial.LEATHER, new ArmorMaterialThermalProfile(0.70D, 0.25D));
        profiles.put(ArmorThermalMaterial.CHAINMAIL, new ArmorMaterialThermalProfile(0.10D, 0.00D));
        profiles.put(ArmorThermalMaterial.IRON, new ArmorMaterialThermalProfile(0.20D, 0.05D));
        profiles.put(ArmorThermalMaterial.GOLD, new ArmorMaterialThermalProfile(0.18D, 0.05D));
        profiles.put(ArmorThermalMaterial.DIAMOND, new ArmorMaterialThermalProfile(0.35D, 0.10D));
        profiles.put(ArmorThermalMaterial.NETHERITE, new ArmorMaterialThermalProfile(0.55D, 0.15D));
        profiles.put(ArmorThermalMaterial.TURTLE_SHELL, new ArmorMaterialThermalProfile(0.30D, 0.40D));
        return new ArmorThermalCatalog(profiles);
    }

    public ArmorMaterialThermalProfile profileFor(ArmorThermalMaterial material) {
        return profiles.get(Objects.requireNonNull(material, "material"));
    }
}
