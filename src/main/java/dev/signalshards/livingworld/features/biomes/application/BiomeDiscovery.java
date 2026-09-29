package dev.signalshards.livingworld.features.biomes.application;

import dev.signalshards.livingworld.features.discovery.application.DiscoveryOutcome;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryRequest;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryService;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryTypeDefinition;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryId;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Contrato da feature de biomas com Discovery.
 *
 * <p>A identidade persistente é sempre a chave namespaced completa. O rótulo
 * legível é apenas apresentação do momento e pode evoluir sem migração de PDC.</p>
 */
public final class BiomeDiscovery {
    private static final Pattern NAMESPACED_KEY = Pattern.compile(
            "[a-z0-9._-]+:[a-z0-9/._-]+"
    );

    public static final DiscoveryType TYPE = new DiscoveryType("biome");

    public static final DiscoveryTypeDefinition DEFINITION =
            new DiscoveryTypeDefinition(
                    TYPE,
                    DiscoveryScope.PERSONAL,
                    "discovery.type.biome"
            );

    private BiomeDiscovery() {
    }

    public static DiscoveryId idFor(String biomeKey) {
        return new DiscoveryId(requireBiomeKey(biomeKey));
    }

    public static String labelFor(String biomeKey) {
        String validated = requireBiomeKey(biomeKey);
        String path = validated.substring(validated.indexOf(':') + 1);
        StringBuilder label = new StringBuilder(path.length());
        boolean capitalize = true;

        for (int index = 0; index < path.length(); index++) {
            char character = path.charAt(index);
            boolean separator = character == '_'
                    || character == '-'
                    || character == '/'
                    || character == '.';
            if (separator) {
                if (!label.isEmpty() && label.charAt(label.length() - 1) != ' ') {
                    label.append(' ');
                }
                capitalize = true;
                continue;
            }

            label.append(capitalize
                    ? Character.toUpperCase(character)
                    : character);
            capitalize = false;
        }

        return label.toString().strip();
    }

    public static DiscoveryOutcome recordEncounter(
            DiscoveryService discovery,
            UUID playerId,
            String biomeKey
    ) {
        Objects.requireNonNull(discovery, "serviço de descobertas");
        Objects.requireNonNull(playerId, "jogador");

        return discovery.discover(new DiscoveryRequest(
                TYPE,
                idFor(biomeKey),
                playerId,
                playerId,
                labelFor(biomeKey)
        ));
    }

    private static String requireBiomeKey(String biomeKey) {
        Objects.requireNonNull(biomeKey, "chave do bioma");
        String normalized = biomeKey.strip();
        if (!NAMESPACED_KEY.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Chave de bioma inválida; esperado namespace:path: " + normalized
            );
        }
        return normalized;
    }
}
