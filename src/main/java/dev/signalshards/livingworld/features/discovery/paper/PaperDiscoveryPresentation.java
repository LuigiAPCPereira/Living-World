package dev.signalshards.livingworld.features.discovery.paper;

import dev.signalshards.livingworld.features.discovery.application.DiscoveryListener;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryUnlocked;
import dev.signalshards.livingworld.features.discovery.presentation.DiscoveryPresentationPolicy;
import dev.signalshards.livingworld.features.discovery.presentation.DiscoveryPresentationRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Objects;

/**
 * Adapta uma descoberta a um título Adventure curto.
 *
 * <p>É a implementação mínima do contrato de apresentação: existe para provar o fluxo
 * completo e para ser substituída por um sistema visual futuro sem tocar no domínio,
 * na aplicação ou na persistência.
 */
public final class PaperDiscoveryPresentation implements DiscoveryListener {
    private static final Title.Times TITLE_TIMES = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofSeconds(3),
            Duration.ofMillis(750)
    );

    private final Server server;
    private final DiscoveryPresentationPolicy policy;

    public PaperDiscoveryPresentation(Server server, DiscoveryPresentationPolicy policy) {
        this.server = Objects.requireNonNull(server, "servidor");
        this.policy = Objects.requireNonNull(policy, "política de apresentação");
    }

    @Override
    public void onDiscovery(DiscoveryUnlocked unlocked) {
        Objects.requireNonNull(unlocked, "descoberta registrada");
        Player player = server.getPlayer(unlocked.discoveredBy());
        if (player == null) {
            return;
        }

        DiscoveryPresentationRequest request = policy.requestFor(unlocked);
        player.showTitle(Title.title(
                Component.text(request.title(), NamedTextColor.GOLD),
                Component.text(request.subtitle(), NamedTextColor.GRAY),
                TITLE_TIMES
        ));
    }
}
