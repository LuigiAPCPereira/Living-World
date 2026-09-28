package dev.signalshards.livingworld.features.discovery.application;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryId;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;

import java.util.Objects;
import java.util.UUID;

/**
 * Entrada do caso de uso "algo foi descoberto".
 *
 * <p>O escopo não entra aqui: ele é resolvido pelo tipo declarado no registro. O
 * rótulo é a owned pela feature que chamou, viaja apenas no evento e nunca é
 * persistido, então um rename posterior não deixa texto antigo.
 *
 * @param type         tipo de descoberta
 * @param id           identidade da coisa descoberta dentro do tipo
 * @param owner        jogador dono (descoberta pessoal) ou mundo dono (descoberta mundial)
 * @param discoveredBy jogador que descobriu agora
 * @param label        nome visível da coisa descoberta no momento da descoberta
 */
public record DiscoveryRequest(
        DiscoveryType type,
        DiscoveryId id,
        UUID owner,
        UUID discoveredBy,
        String label
) {
    private static final int MAX_LABEL_LENGTH = 96;

    public DiscoveryRequest {
        Objects.requireNonNull(type, "tipo de descoberta");
        Objects.requireNonNull(id, "identificador de descoberta");
        Objects.requireNonNull(owner, "dono da descoberta");
        Objects.requireNonNull(discoveredBy, "jogador que descobriu");
        Objects.requireNonNull(label, "rótulo da descoberta");

        label = label.strip();
        if (label.isEmpty()) {
            throw new IllegalArgumentException("O rótulo da descoberta não pode ficar vazio");
        }
        if (label.length() > MAX_LABEL_LENGTH) {
            throw new IllegalArgumentException(
                    "O rótulo da descoberta não pode ultrapassar " + MAX_LABEL_LENGTH + " caracteres"
            );
        }
    }
}
