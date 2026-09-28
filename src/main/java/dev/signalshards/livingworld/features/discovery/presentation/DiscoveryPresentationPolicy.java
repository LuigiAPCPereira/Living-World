package dev.signalshards.livingworld.features.discovery.presentation;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryRegistry;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryUnlocked;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;

import java.util.Objects;

/**
 * Converte uma descoberta em um pedido de apresentação localizado.
 *
 * <p>É uma função pura sobre o evento, sem Bukkit, para que o texto de uma descoberta
 * seja verificável sem servidor e para que um sistema visual futuro reaproveite o
 * mesmo pedido em vez de reescrever a frase.
 */
public final class DiscoveryPresentationPolicy {
    private final MessageCatalog messages;
    private final DiscoveryRegistry registry;

    public DiscoveryPresentationPolicy(MessageCatalog messages, DiscoveryRegistry registry) {
        this.messages = Objects.requireNonNull(messages, "catálogo de mensagens");
        this.registry = Objects.requireNonNull(registry, "registro de descobertas");
    }

    public DiscoveryPresentationRequest requestFor(DiscoveryUnlocked unlocked) {
        Objects.requireNonNull(unlocked, "descoberta registrada");
        DiscoveryRecord record = unlocked.record();

        return new DiscoveryPresentationRequest(
                record.type(),
                record.scope(),
                messages.text("discovery.unlocked-title"),
                messages.text(
                        "discovery.unlocked-subtitle",
                        unlocked.label(),
                        messages.text(registry.require(record.type()).labelMessageKey())
                )
        );
    }
}
