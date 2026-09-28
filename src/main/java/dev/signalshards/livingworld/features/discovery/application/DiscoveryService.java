package dev.signalshards.livingworld.features.discovery.application;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.persistence.PlayerDiscoveryStore;
import dev.signalshards.livingworld.features.discovery.persistence.WorldDiscoveryStore;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Dono do caso de uso "algo foi descoberto".
 *
 * <p>Responsabilidades, e apenas elas: resolver a política do tipo declarado, gravar o
 * fato no armazenamento do escopo correto, decidir se é a primeira vez e publicar o
 * evento. Não conhece Paper, não conhece i18n e não conhece as features que produzem
 * as descobertas.
 */
public final class DiscoveryService {
    private final DiscoveryRegistry registry;
    private final DiscoveryEventPublisher publisher;
    private final PlayerDiscoveryStore playerStore;
    private final WorldDiscoveryStore worldStore;

    public DiscoveryService(
            DiscoveryRegistry registry,
            DiscoveryEventPublisher publisher,
            PlayerDiscoveryStore playerStore,
            WorldDiscoveryStore worldStore
    ) {
        this.registry = Objects.requireNonNull(registry, "registro de descobertas");
        this.publisher = Objects.requireNonNull(publisher, "publicador de descobertas");
        this.playerStore = Objects.requireNonNull(playerStore, "descobertas do jogador");
        this.worldStore = Objects.requireNonNull(worldStore, "descobertas do mundo");
    }

    /**
     * Registra uma descoberta e publica o evento apenas na primeira vez.
     *
     * <p>Repetir o mesmo {@code (type, id)} para o mesmo dono é idempotente e
     * silencioso: reencontrar um lugar conhecido não é uma conquista nova.
     */
    public DiscoveryOutcome discover(DiscoveryRequest request) {
        Objects.requireNonNull(request, "pedido de descoberta");

        DiscoveryTypeDefinition definition = registry.require(request.type());
        DiscoveryScope scope = definition.scope();
        if (scope == DiscoveryScope.PERSONAL && !request.owner().equals(request.discoveredBy())) {
            throw new IllegalArgumentException(
                    "Uma descoberta pessoal pertence ao jogador que a descobrir"
            );
        }

        DiscoveryRecord record = new DiscoveryRecord(
                request.type(),
                request.id(),
                scope,
                request.owner()
        );
        boolean firstDiscovery = add(record);

        if (firstDiscovery) {
            publisher.publish(
                    new DiscoveryUnlocked(record, request.discoveredBy(), request.label())
            );
        }
        return new DiscoveryOutcome(record, firstDiscovery);
    }

    /**
     * Fatos já conhecidos de um dono, na ordem em que foram descobertos.
     */
    public Set<DiscoveryRecord> discoveries(UUID owner, DiscoveryScope scope) {
        Objects.requireNonNull(owner, "dono da descoberta");
        Objects.requireNonNull(scope, "escopo de descoberta");

        return switch (scope) {
            case PERSONAL -> playerStore.load(owner);
            case WORLD -> worldStore.load(owner);
        };
    }

    private boolean add(DiscoveryRecord record) {
        return switch (record.scope()) {
            case PERSONAL -> playerStore.add(record.owner(), record);
            case WORLD -> worldStore.add(record.owner(), record);
        };
    }
}
