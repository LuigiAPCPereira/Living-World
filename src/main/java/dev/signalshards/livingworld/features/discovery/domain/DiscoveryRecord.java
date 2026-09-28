package dev.signalshards.livingworld.features.discovery.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Fato durável de uma descoberta.
 *
 * <p>A identidade persistida é o par {@code (type, id)}: tipo declara o que foi
 * descoberto e o id identifica a coisa dentro desse tipo.
 *
 * <p>O registro guarda apenas identidade e posse. Nunca guarda uma cópia do nome da
 * coisa descoberta: a feature dona do nome continua sendo a única fonte dele, então um
 * rename não deixa dado antigo para trás.
 *
 * @param type  tipo da descoberta
 * @param id    identidade da descoberta dentro do tipo
 * @param scope quem possui o fato
 * @param owner UUID do jogador em {@link DiscoveryScope#PERSONAL} ou do mundo em
 *              {@link DiscoveryScope#WORLD}
 */
public record DiscoveryRecord(
        DiscoveryType type,
        DiscoveryId id,
        DiscoveryScope scope,
        UUID owner
) {
    public DiscoveryRecord {
        Objects.requireNonNull(type, "tipo de descoberta");
        Objects.requireNonNull(id, "identificador de descoberta");
        Objects.requireNonNull(scope, "escopo de descoberta");
        Objects.requireNonNull(owner, "dono da descoberta");
    }
}
