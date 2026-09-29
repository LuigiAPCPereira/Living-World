package dev.signalshards.livingworld.features.landmarks.application;

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
 * Contrato de landmarks gerados com o núcleo de Discovery.
 *
 * <p>A identidade persistida é o UUID da instância gerada, nunca coordenadas.
 * A chave da estrutura serve apenas como contexto/rótulo do evento e pode mudar
 * de apresentação sem migrar a memória de Discovery.</p>
 */
public final class LandmarkDiscovery {
    private static final Pattern NAMESPACED_KEY = Pattern.compile(
            "[a-z0-9._-]+:[a-z0-9/._-]+"
    );

    public static final DiscoveryType TYPE = new DiscoveryType("landmark");

    public static final DiscoveryTypeDefinition DEFINITION =
            new DiscoveryTypeDefinition(
                    TYPE,
                    DiscoveryScope.WORLD,
                    "discovery.type.landmark"
            );

    private LandmarkDiscovery() {
    }

    public static DiscoveryId idFor(UUID landmarkId) {
        return new DiscoveryId(
                Objects.requireNonNull(landmarkId, "landmark").toString()
        );
    }

    public static String labelFor(String structureKey) {
        String validated = requireStructureKey(structureKey);
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
                if (!label.isEmpty()
                        && label.charAt(label.length() - 1) != ' ') {
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
            UUID worldId,
            UUID discoveredBy,
            UUID landmarkId,
            String structureKey
    ) {
        Objects.requireNonNull(discovery, "serviço de descobertas");
        Objects.requireNonNull(worldId, "mundo");
        Objects.requireNonNull(discoveredBy, "jogador");

        return discovery.discover(new DiscoveryRequest(
                TYPE,
                idFor(landmarkId),
                worldId,
                discoveredBy,
                labelFor(structureKey)
        ));
    }

    private static String requireStructureKey(String structureKey) {
        Objects.requireNonNull(structureKey, "chave da estrutura");
        String normalized = structureKey.strip();
        if (!NAMESPACED_KEY.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Chave de estrutura inválida; esperado namespace:path: "
                            + normalized
            );
        }
        return normalized;
    }
}
