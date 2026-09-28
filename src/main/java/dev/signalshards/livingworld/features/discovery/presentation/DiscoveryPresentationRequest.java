package dev.signalshards.livingworld.features.discovery.presentation;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;

import java.util.Objects;

/**
 * Contrato de apresentação de uma descoberta.
 *
 * <p>É propositalmente um valor puro, sem Bukkit: um sistema visual futuro consome
 * este pedido e decide o seu próprio formato, sem que o Discovery saiba ou precise
 * saber. O adaptador atual apenas transforma o pedido em um título Adventure.
 *
 * @param type     tipo da descoberta
 * @param scope    se a descoberta pertence ao jogador ou ao mundo
 * @param title    título localizado
 * @param subtitle legenda localizada
 */
public record DiscoveryPresentationRequest(
        DiscoveryType type,
        DiscoveryScope scope,
        String title,
        String subtitle
) {
    public DiscoveryPresentationRequest {
        Objects.requireNonNull(type, "tipo de descoberta");
        Objects.requireNonNull(scope, "escopo de descoberta");
        Objects.requireNonNull(title, "título da descoberta");
        Objects.requireNonNull(subtitle, "legenda da descoberta");
    }
}
