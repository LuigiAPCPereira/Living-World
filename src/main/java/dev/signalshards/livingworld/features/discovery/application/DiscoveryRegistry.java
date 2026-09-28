package dev.signalshards.livingworld.features.discovery.application;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Catálogo imutável dos tipos de descoberta declarados pelo Living World.
 *
 * <p>É um catálogo de política, não um localizador de serviços: guarda apenas a
 * definição de cada tipo. Um tipo não declarado falha fechada, para que uma
 * capability nova nunca grave uma descoberta sem escopo definido.
 */
public final class DiscoveryRegistry {
    private final Map<DiscoveryType, DiscoveryTypeDefinition> definitions;

    public DiscoveryRegistry(Collection<DiscoveryTypeDefinition> definitions) {
        Objects.requireNonNull(definitions, "definições de descoberta");
        Map<DiscoveryType, DiscoveryTypeDefinition> declared = new LinkedHashMap<>();
        for (DiscoveryTypeDefinition definition : definitions) {
            Objects.requireNonNull(definition, "definição de descoberta");
            DiscoveryTypeDefinition previous = declared.putIfAbsent(definition.type(), definition);
            if (previous != null) {
                throw new IllegalArgumentException(
                        "O tipo de descoberta está declarado mais de uma vez: " + definition.type()
                );
            }
        }
        this.definitions = Map.copyOf(declared);
    }

    public boolean declares(DiscoveryType type) {
        return definitions.containsKey(Objects.requireNonNull(type, "tipo de descoberta"));
    }

    public DiscoveryTypeDefinition require(DiscoveryType type) {
        Objects.requireNonNull(type, "tipo de descoberta");
        DiscoveryTypeDefinition definition = definitions.get(type);
        if (definition == null) {
            throw new IllegalStateException(
                    "O tipo de descoberta não foi declarado pelo Living World: " + type
            );
        }
        return definition;
    }
}
