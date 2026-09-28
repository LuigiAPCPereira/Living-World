package dev.signalshards.livingworld.core.companion.paper;

import dev.signalshards.livingworld.core.companion.CompanionPackCompatibility;
import dev.signalshards.livingworld.core.companion.CompanionPackContractPolicy;
import dev.signalshards.livingworld.core.companion.CompanionPackKind;
import dev.signalshards.livingworld.core.companion.CompanionPackManifest;
import dev.signalshards.livingworld.core.companion.CompanionPackManifestSource;
import dev.signalshards.livingworld.core.companion.CompanionPackRequirement;
import dev.signalshards.livingworld.core.companion.DatapackRuntimeStatus;
import dev.signalshards.livingworld.core.companion.LivingWorldCompanionContracts;
import dev.signalshards.livingworld.core.module.LivingWorldModule;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Verificação de bootstrap para datapack previamente instalado pelo operador.
 *
 * <p>Não instala, habilita, atualiza ou recarrega datapacks.</p>
 */
public final class PaperDatapackContractModule implements LivingWorldModule {
    private final Plugin plugin;
    private final PaperDatapackContractSettings settings;
    private final CompanionPackManifestSource manifestSource;
    private final CompanionPackContractPolicy contractPolicy;
    private final PaperDatapackRuntimeResolver runtimeResolver;
    private DatapackRuntimeStatus status = DatapackRuntimeStatus.NOT_CONFIGURED;

    public PaperDatapackContractModule(
            Plugin plugin,
            PaperDatapackContractSettings settings,
            CompanionPackManifestSource manifestSource
    ) {
        this(
                plugin,
                settings,
                manifestSource,
                new CompanionPackContractPolicy(),
                new PaperDatapackRuntimeResolver()
        );
    }

    PaperDatapackContractModule(
            Plugin plugin,
            PaperDatapackContractSettings settings,
            CompanionPackManifestSource manifestSource,
            CompanionPackContractPolicy contractPolicy,
            PaperDatapackRuntimeResolver runtimeResolver
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.settings = Objects.requireNonNull(settings, "configuração do datapack");
        this.manifestSource = Objects.requireNonNull(
                manifestSource,
                "fonte de manifest"
        );
        this.contractPolicy = Objects.requireNonNull(
                contractPolicy,
                "policy de contrato"
        );
        this.runtimeResolver = Objects.requireNonNull(
                runtimeResolver,
                "resolver runtime"
        );
    }

    @Override
    public void enable() {
        if (!settings.enabled()) {
            status = DatapackRuntimeStatus.NOT_CONFIGURED;
            return;
        }

        Optional<CompanionPackManifest> manifest;
        try {
            manifest = manifestSource.load();
        } catch (IOException | IllegalArgumentException exception) {
            handleFailure(
                    DatapackRuntimeStatus.INCOMPATIBLE,
                    "Falha ao ler manifest do datapack Living World",
                    exception
            );
            return;
        }

        CompanionPackCompatibility compatibility = manifest.isEmpty()
                ? missingCompatibility()
                : compatibilityFor(manifest.orElseThrow());

        if (compatibility == CompanionPackCompatibility.REQUIRED_MISSING) {
            handleFailure(
                    DatapackRuntimeStatus.REQUIRED_MISSING,
                    "Manifest obrigatório do datapack Living World ausente",
                    null
            );
            return;
        }
        if (compatibility == CompanionPackCompatibility.OPTIONAL_MISSING) {
            handleFailure(
                    DatapackRuntimeStatus.OPTIONAL_MISSING,
                    "Datapack Living World não configurado; usando fallback do plugin",
                    null
            );
            return;
        }

        status = runtimeResolver.resolve(
                plugin.getServer().getDatapackManager(),
                settings.paperName(),
                compatibility
        );
        if (status == DatapackRuntimeStatus.OPTIONAL_MISSING
                && settings.required()) {
            status = DatapackRuntimeStatus.REQUIRED_MISSING;
        }
        if (status != DatapackRuntimeStatus.COMPATIBLE_ENABLED) {
            handleFailure(
                    status,
                    "Datapack Living World não está compatível+enabled: " + status,
                    null
            );
        }
    }

    private CompanionPackCompatibility missingCompatibility() {
        return contractPolicy.evaluateMissing(requirement());
    }

    private CompanionPackCompatibility compatibilityFor(
            CompanionPackManifest manifest
    ) {
        return contractPolicy.evaluate(requirement(), manifest);
    }

    private CompanionPackRequirement requirement() {
        return new CompanionPackRequirement(
                CompanionPackKind.DATAPACK,
                LivingWorldCompanionContracts.DATAPACK_SCHEMA,
                settings.required()
        );
    }

    @Override
    public void disable() {
        status = DatapackRuntimeStatus.NOT_CONFIGURED;
    }

    public DatapackRuntimeStatus status() {
        return status;
    }

    private void handleFailure(
            DatapackRuntimeStatus newStatus,
            String message,
            Exception exception
    ) {
        status = newStatus;
        if (settings.required()) {
            if (exception == null) {
                throw new IllegalStateException(message);
            }
            throw new IllegalStateException(message, exception);
        }
        if (exception == null) {
            plugin.getLogger().warning(message);
        } else {
            plugin.getLogger().log(Level.WARNING, message, exception);
        }
    }
}
