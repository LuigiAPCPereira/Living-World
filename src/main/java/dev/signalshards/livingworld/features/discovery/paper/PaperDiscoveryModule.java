package dev.signalshards.livingworld.features.discovery.paper;

import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryEventPublisher;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryListener;

import java.util.Objects;

/**
 * Ciclo de vida da apresentação de descobertas.
 *
 * <p>Assinar e cancelar a assinatura do listener é tudo o que este módulo possui: o
 * registro das descobertas em si pertence ao serviço e não tem tarefa, listener nem
 * estado de tempo de execução próprio.
 */
public final class PaperDiscoveryModule implements LivingWorldModule {
    private final DiscoveryEventPublisher publisher;
    private final DiscoveryListener presentation;
    private final boolean enabled;

    public PaperDiscoveryModule(
            DiscoveryEventPublisher publisher,
            DiscoveryListener presentation,
            boolean enabled
    ) {
        this.publisher = Objects.requireNonNull(publisher, "publicador de descobertas");
        this.presentation = Objects.requireNonNull(presentation, "apresentação de descoberta");
        this.enabled = enabled;
    }

    @Override
    public void enable() {
        if (enabled) {
            publisher.subscribe(presentation);
        }
    }

    @Override
    public void disable() {
        publisher.unsubscribe(presentation);
    }
}
