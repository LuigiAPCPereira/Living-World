package dev.signalshards.livingworld.features.landmarks.application;

import java.util.Objects;
import java.util.UUID;

/**
 * Landmark gerado já resolvido pelo adapter Paper.
 *
 * <p>O UUID é a identidade persistente da instância. A chave da estrutura é
 * apenas contexto para apresentação e nunca substitui a identidade.</p>
 */
public record ResolvedLandmark(
        UUID id,
        String structureKey
) {
    public ResolvedLandmark {
        Objects.requireNonNull(id, "identidade do landmark");
        Objects.requireNonNull(structureKey, "chave da estrutura");
        structureKey = structureKey.strip();
        if (structureKey.isEmpty()) {
            throw new IllegalArgumentException(
                    "A chave da estrutura não pode ficar vazia"
            );
        }
    }
}
