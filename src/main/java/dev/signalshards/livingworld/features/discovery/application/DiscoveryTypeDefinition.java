package dev.signalshards.livingworld.features.discovery.application;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;

import java.util.Objects;

/**
 * Política declarada por um tipo de descoberta.
 *
 * <p>O escopo pertence ao tipo, não ao chamador: é isso que impede uma feature de
 * gravar uma descoberta pessoal no armazenamento do mundo (ou o contrário) e mantém a
 * decisão num único lugar.
 *
 * @param type            tipo de descoberta
 * @param scope           dono do fato para qualquer descoberta desse tipo
 * @param labelMessageKey chave de mensagem do rótulo do tipo, usada pela apresentação
 */
public record DiscoveryTypeDefinition(
        DiscoveryType type,
        DiscoveryScope scope,
        String labelMessageKey
) {
    public DiscoveryTypeDefinition {
        Objects.requireNonNull(type, "tipo de descoberta");
        Objects.requireNonNull(scope, "escopo de descoberta");
        Objects.requireNonNull(labelMessageKey, "chave de mensagem do tipo");
        labelMessageKey = labelMessageKey.strip();
        if (labelMessageKey.isEmpty()) {
            throw new IllegalArgumentException("A chave de mensagem do tipo não pode ficar vazia");
        }
    }
}
