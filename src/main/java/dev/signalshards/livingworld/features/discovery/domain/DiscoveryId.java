package dev.signalshards.livingworld.features.discovery.domain;

import java.util.Objects;

/**
 * Identidade da coisa descoberta dentro do seu {@link DiscoveryType}.
 *
 * <p>A identidade nunca é coordenada nem nome visível: uma waystone usa o UUID, um
 * bioma usaria a chave do bioma. Renomear a coisa descoberta não altera esta
 * identidade.
 */
public record DiscoveryId(String value) {
    private static final int MAX_LENGTH = 96;

    public DiscoveryId {
        Objects.requireNonNull(value, "identificador de descoberta");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("O identificador de descoberta não pode ficar vazio");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "O identificador de descoberta não pode ultrapassar " + MAX_LENGTH + " caracteres"
            );
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
