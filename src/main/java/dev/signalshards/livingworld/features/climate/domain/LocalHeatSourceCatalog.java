package dev.signalshards.livingworld.features.climate.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Catálogo configurável de potência/alcance por fonte térmica.
 *
 * <p>Os defaults expressam identidade Vanilla+ e ordem relativa de força;
 * não são medições físicas reais.</p>
 */
public final class LocalHeatSourceCatalog {
    private final Map<LocalHeatSourceType, LocalHeatSourceProfile> profiles;

    public LocalHeatSourceCatalog(Map<LocalHeatSourceType, LocalHeatSourceProfile> profiles) {
        Objects.requireNonNull(profiles, "perfis de fontes térmicas");
        EnumMap<LocalHeatSourceType, LocalHeatSourceProfile> copy =
                new EnumMap<>(LocalHeatSourceType.class);
        copy.putAll(profiles);
        for (LocalHeatSourceType type : LocalHeatSourceType.values()) {
            if (!copy.containsKey(type)) {
                throw new IllegalArgumentException("Perfil ausente para " + type);
            }
            Objects.requireNonNull(copy.get(type), "perfil de " + type);
        }
        this.profiles = Map.copyOf(copy);
    }

    public static LocalHeatSourceCatalog livingWorldDefaults() {
        EnumMap<LocalHeatSourceType, LocalHeatSourceProfile> profiles =
                new EnumMap<>(LocalHeatSourceType.class);
        profiles.put(LocalHeatSourceType.TORCH, new LocalHeatSourceProfile(0.0020D, 3.5D));
        profiles.put(LocalHeatSourceType.LANTERN, new LocalHeatSourceProfile(0.0015D, 3.5D));
        profiles.put(LocalHeatSourceType.LIT_FURNACE, new LocalHeatSourceProfile(0.0035D, 4.0D));
        profiles.put(LocalHeatSourceType.LIT_SMOKER, new LocalHeatSourceProfile(0.0035D, 4.0D));
        profiles.put(LocalHeatSourceType.LIT_BLAST_FURNACE, new LocalHeatSourceProfile(0.0040D, 4.0D));
        profiles.put(LocalHeatSourceType.FIRE, new LocalHeatSourceProfile(0.0060D, 5.0D));
        profiles.put(LocalHeatSourceType.CAMPFIRE, new LocalHeatSourceProfile(0.0080D, 6.0D));
        profiles.put(LocalHeatSourceType.LAVA, new LocalHeatSourceProfile(0.0150D, 8.0D));
        profiles.put(LocalHeatSourceType.MAGMA_BLOCK, new LocalHeatSourceProfile(0.0030D, 2.5D));
        return new LocalHeatSourceCatalog(profiles);
    }

    public LocalHeatSourceProfile profileFor(LocalHeatSourceType type) {
        return profiles.get(Objects.requireNonNull(type, "tipo de fonte térmica"));
    }
}
