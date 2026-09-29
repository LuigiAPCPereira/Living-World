package dev.signalshards.livingworld.core.companion.paper;

import dev.signalshards.livingworld.core.companion.CompanionPackCompatibility;
import dev.signalshards.livingworld.core.companion.CompanionPackContractPolicy;
import dev.signalshards.livingworld.core.companion.CompanionPackKind;
import dev.signalshards.livingworld.core.companion.CompanionPackManifest;
import dev.signalshards.livingworld.core.companion.CompanionPackManifestSource;
import dev.signalshards.livingworld.core.companion.CompanionPackRequirement;
import dev.signalshards.livingworld.core.companion.LivingWorldCompanionContracts;
import dev.signalshards.livingworld.core.companion.ResourcePackSessionStore;
import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.core.status.EnvironmentalMetric;
import dev.signalshards.livingworld.core.status.EnvironmentalPerformanceMetrics;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Delivery opt-in de um único resource pack Living World.
 */
public final class PaperResourcePackDeliveryModule
        implements LivingWorldModule, Listener {
    private final Plugin plugin;
    private final PaperResourcePackDeliverySettings settings;
    private final ResourcePackSessionStore sessions;
    private final CompanionPackManifestSource manifestSource;
    private final CompanionPackContractPolicy contractPolicy;
    private final Supplier<UUID> requestIds;
    private final EnvironmentalPerformanceMetrics performanceMetrics;
    private boolean active;
    private boolean deliveryReady;

    public PaperResourcePackDeliveryModule(
            Plugin plugin,
            PaperResourcePackDeliverySettings settings,
            ResourcePackSessionStore sessions,
            CompanionPackManifestSource manifestSource
    ) {
        this(
                plugin,
                settings,
                sessions,
                manifestSource,
                new CompanionPackContractPolicy(),
                UUID::randomUUID,
                new EnvironmentalPerformanceMetrics()
        );
    }

    public PaperResourcePackDeliveryModule(
            Plugin plugin,
            PaperResourcePackDeliverySettings settings,
            ResourcePackSessionStore sessions,
            CompanionPackManifestSource manifestSource,
            EnvironmentalPerformanceMetrics performanceMetrics
    ) {
        this(
                plugin,
                settings,
                sessions,
                manifestSource,
                new CompanionPackContractPolicy(),
                UUID::randomUUID,
                performanceMetrics
        );
    }

    PaperResourcePackDeliveryModule(
            Plugin plugin,
            PaperResourcePackDeliverySettings settings,
            ResourcePackSessionStore sessions,
            CompanionPackManifestSource manifestSource,
            CompanionPackContractPolicy contractPolicy,
            Supplier<UUID> requestIds
    ) {
        this(
                plugin,
                settings,
                sessions,
                manifestSource,
                contractPolicy,
                requestIds,
                new EnvironmentalPerformanceMetrics()
        );
    }

    PaperResourcePackDeliveryModule(
            Plugin plugin,
            PaperResourcePackDeliverySettings settings,
            ResourcePackSessionStore sessions,
            CompanionPackManifestSource manifestSource,
            CompanionPackContractPolicy contractPolicy,
            Supplier<UUID> requestIds,
            EnvironmentalPerformanceMetrics performanceMetrics
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.settings = Objects.requireNonNull(settings, "configuração de delivery");
        this.sessions = Objects.requireNonNull(sessions, "sessões de resource pack");
        this.manifestSource = Objects.requireNonNull(
                manifestSource,
                "fonte de manifest"
        );
        this.contractPolicy = Objects.requireNonNull(
                contractPolicy,
                "policy de contrato"
        );
        this.requestIds = Objects.requireNonNull(requestIds, "gerador de request IDs");
        this.performanceMetrics = Objects.requireNonNull(
                performanceMetrics,
                "métricas de performance"
        );
    }

    @Override
    public void enable() {
        if (!settings.enabled()) {
            return;
        }
        deliveryReady = verifyManifest();
        if (!deliveryReady) {
            return;
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        active = true;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            request(player);
        }
    }

    @Override
    public void disable() {
        active = false;
        deliveryReady = false;
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (active && deliveryReady) {
            request(event.getPlayer());
        }
    }

    private boolean verifyManifest() {
        final Optional<CompanionPackManifest> manifest;
        try {
            manifest = manifestSource.load();
        } catch (IOException | IllegalArgumentException exception) {
            return handleUnavailable(
                    "Falha ao ler manifest do resource pack",
                    exception
            );
        }
        if (manifest.isEmpty()) {
            return handleUnavailable(
                    "Manifest do resource pack Living World ausente",
                    null
            );
        }

        CompanionPackRequirement requirement = new CompanionPackRequirement(
                CompanionPackKind.RESOURCE_PACK,
                LivingWorldCompanionContracts.RESOURCE_PACK_SCHEMA,
                settings.required()
        );
        CompanionPackCompatibility compatibility = contractPolicy.evaluate(
                requirement,
                manifest.orElseThrow()
        );
        if (compatibility != CompanionPackCompatibility.COMPATIBLE) {
            return handleUnavailable(
                    "Manifest do resource pack incompatível: " + compatibility,
                    null
            );
        }
        return true;
    }

    private boolean handleUnavailable(String message, Exception exception) {
        if (settings.required()) {
            if (exception == null) {
                throw new IllegalStateException(message);
            }
            throw new IllegalStateException(message, exception);
        }
        if (exception == null) {
            plugin.getLogger().warning(message + "; usando fallback vanilla");
        } else {
            plugin.getLogger().log(
                    Level.WARNING,
                    message + "; usando fallback vanilla",
                    exception
            );
        }
        return false;
    }

    private void request(Player player) {
        UUID requestId = requestIds.get();
        sessions.requested(
                player.getUniqueId(),
                requestId,
                settings.required()
        );
        try {
            ResourcePackInfo info = ResourcePackInfo.resourcePackInfo(
                    requestId,
                    URI.create(settings.url()),
                    settings.sha1()
            );
            ResourcePackRequest request = ResourcePackRequest
                    .resourcePackRequest()
                    .packs(info)
                    .replace(true)
                    .required(settings.required())
                    .prompt(Component.text(settings.prompt()))
                    .build();
            player.sendResourcePacks(request);
            performanceMetrics.increment(
                    EnvironmentalMetric.RESOURCE_PACK_REQUESTS
            );
        } catch (RuntimeException exception) {
            sessions.reset(player.getUniqueId());
            plugin.getLogger().log(
                    Level.WARNING,
                    "Falha ao solicitar resource pack ao jogador "
                            + player.getUniqueId(),
                    exception
            );
            if (settings.required()) {
                throw exception;
            }
        }
    }
}
