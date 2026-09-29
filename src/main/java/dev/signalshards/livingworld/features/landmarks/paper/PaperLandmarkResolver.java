package dev.signalshards.livingworld.features.landmarks.paper;

import dev.signalshards.livingworld.features.landmarks.application.ResolvedLandmark;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Boundary Paper que traduz uma estrutura gerada elegível em identidade de
 * landmark estável.
 *
 * <p>A elegibilidade vem exclusivamente de {@code #livingworld:landmarks}.
 * A tag é resolvida uma vez na construção do adapter. Tag ausente/vazia deixa o
 * resolver inerte. O UUID da instância vive no PDC do {@link GeneratedStructure},
 * que o Paper persiste no NBT do StructureStart.</p>
 */
public final class PaperLandmarkResolver {
    private static final TagKey<Structure> LANDMARKS_TAG =
            TagKey.create(RegistryKey.STRUCTURE, "livingworld:landmarks");

    private final Registry<Structure> structureRegistry;
    private final Set<NamespacedKey> eligibleStructureKeys;
    private final NamespacedKey landmarkIdKey;

    public PaperLandmarkResolver(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.structureRegistry = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.STRUCTURE);
        this.eligibleStructureKeys = loadEligibleKeys(structureRegistry);
        this.landmarkIdKey = new NamespacedKey(
                plugin,
                "discovery-landmark-id"
        );
    }

    public Optional<ResolvedLandmark> resolve(
            GeneratedStructure generatedStructure
    ) {
        Objects.requireNonNull(generatedStructure, "estrutura gerada");

        Structure structure = generatedStructure.getStructure();
        NamespacedKey structureKey = structureRegistry.getKey(structure);
        if (structureKey == null
                || !eligibleStructureKeys.contains(structureKey)) {
            return Optional.empty();
        }

        UUID landmarkId = readOrCreateId(
                generatedStructure.getPersistentDataContainer()
        );
        return Optional.of(new ResolvedLandmark(
                landmarkId,
                structureKey.toString()
        ));
    }

    public int eligibleStructureCount() {
        return eligibleStructureKeys.size();
    }

    private Set<NamespacedKey> loadEligibleKeys(
            Registry<Structure> registry
    ) {
        if (!registry.hasTag(LANDMARKS_TAG)) {
            return Set.of();
        }

        return registry.getTagValues(LANDMARKS_TAG)
                .stream()
                .map(registry::getKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    private UUID readOrCreateId(PersistentDataContainer container) {
        String stored = container.get(
                landmarkIdKey,
                PersistentDataType.STRING
        );
        if (stored != null) {
            try {
                return UUID.fromString(stored);
            } catch (IllegalArgumentException ignored) {
                // Corrige apenas o valor inválido desta instância.
            }
        }

        UUID created = UUID.randomUUID();
        container.set(
                landmarkIdKey,
                PersistentDataType.STRING,
                created.toString()
        );
        return created;
    }
}
