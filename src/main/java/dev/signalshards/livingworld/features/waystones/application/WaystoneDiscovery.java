package dev.signalshards.livingworld.features.waystones.application;

import dev.signalshards.livingworld.features.discovery.application.DiscoveryTypeDefinition;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryOutcome;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryRequest;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryService;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryId;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;
import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;

import java.util.Objects;
import java.util.UUID;

/**
 * A declaração de descoberta da feature de waystones.
 *
 * <p>Vive aqui, e não dentro do Discovery, porque o tipo, o escopo pessoal e a
 * TRADUÇÃO de uma waystone para identidade de descoberta são fatos da feature de
 * waystones. O núcleo do Discovery continua sem conhecer as features que o usam.
 */
public final class WaystoneDiscovery {
    public static final DiscoveryType TYPE = new DiscoveryType("waystone");

    public static final DiscoveryTypeDefinition DEFINITION = new DiscoveryTypeDefinition(
            TYPE,
            DiscoveryScope.PERSONAL,
            "discovery.type.waystone"
    );

    private WaystoneDiscovery() {
    }

    /**
     * A identidade da descoberta é o UUID da waystone, então renomear não altera o
     * registro e recriar a âncora cria uma descoberta diferente.
     */
    public static DiscoveryId idFor(WaystoneId waystoneId) {
        return new DiscoveryId(
                Objects.requireNonNull(waystoneId, "waystone").value().toString()
        );
    }

    /**
     * Registra a memória pessoal de uma interação válida com uma Waystone.
     *
     * <p>Esta chamada é propositalmente independente do retorno de
     * {@link WaystoneService#activate(UUID, WaystoneId)}. Assim jogadores que já
     * possuíam acesso antes da introdução do Discovery recebem o backfill na primeira
     * interação posterior; o próprio {@link DiscoveryService} garante idempotência.
     */
    public static DiscoveryOutcome recordInteraction(
            DiscoveryService discovery,
            UUID playerId,
            Waystone waystone
    ) {
        Objects.requireNonNull(discovery, "serviço de descobertas");
        Objects.requireNonNull(playerId, "jogador");
        Objects.requireNonNull(waystone, "waystone");

        return discovery.discover(new DiscoveryRequest(
                TYPE,
                idFor(waystone.id()),
                playerId,
                playerId,
                waystone.name()
        ));
    }
}
