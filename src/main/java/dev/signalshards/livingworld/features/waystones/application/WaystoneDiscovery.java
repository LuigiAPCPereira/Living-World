package dev.signalshards.livingworld.features.waystones.application;

import dev.signalshards.livingworld.features.discovery.application.DiscoveryTypeDefinition;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryId;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;

import java.util.Objects;

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
}
