package dev.signalshards.livingworld.core.companion.paper;

import dev.signalshards.livingworld.core.companion.CompanionPackKind;
import dev.signalshards.livingworld.core.companion.CompanionPackManifest;
import dev.signalshards.livingworld.core.companion.DatapackRuntimeStatus;
import io.papermc.paper.datapack.Datapack;
import io.papermc.paper.datapack.DatapackManager;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaperDatapackContractModuleTest {
    @Test
    void disabledNaoConsultaManifestNemPaper() {
        AtomicInteger managerReads = new AtomicInteger();
        PaperDatapackContractModule module = module(
                PaperDatapackContractSettings.disabled(),
                () -> {
                    throw new AssertionError("Manifest não deveria ser lido");
                },
                managerReads,
                null
        );

        module.enable();

        assertEquals(DatapackRuntimeStatus.NOT_CONFIGURED, module.status());
        assertEquals(0, managerReads.get());
    }

    @Test
    void manifestAusenteOpcionalUsaFallbackSemConsultarPaper() {
        AtomicInteger managerReads = new AtomicInteger();
        PaperDatapackContractModule module = module(
                new PaperDatapackContractSettings(
                        true,
                        "file/living-world",
                        false
                ),
                Optional::empty,
                managerReads,
                null
        );

        module.enable();

        assertEquals(DatapackRuntimeStatus.OPTIONAL_MISSING, module.status());
        assertEquals(0, managerReads.get());
    }

    @Test
    void manifestAusenteObrigatorioFalha() {
        PaperDatapackContractModule module = module(
                new PaperDatapackContractSettings(
                        true,
                        "file/living-world",
                        true
                ),
                Optional::empty,
                new AtomicInteger(),
                null
        );

        assertThrows(IllegalStateException.class, module::enable);
        assertEquals(DatapackRuntimeStatus.REQUIRED_MISSING, module.status());
    }

    @Test
    void manifestCompativelEPackEnabledPassam() {
        AtomicInteger managerReads = new AtomicInteger();
        PaperDatapackContractModule module = module(
                new PaperDatapackContractSettings(
                        true,
                        "file/living-world",
                        true
                ),
                () -> Optional.of(manifest()),
                managerReads,
                pack(true)
        );

        module.enable();

        assertEquals(
                DatapackRuntimeStatus.COMPATIBLE_ENABLED,
                module.status()
        );
        assertEquals(1, managerReads.get());
    }

    @Test
    void packNaoDescobertoObrigatorioEhRequiredMissing() {
        PaperDatapackContractModule module = module(
                new PaperDatapackContractSettings(
                        true,
                        "file/living-world",
                        true
                ),
                () -> Optional.of(manifest()),
                new AtomicInteger(),
                null
        );

        assertThrows(IllegalStateException.class, module::enable);
        assertEquals(DatapackRuntimeStatus.REQUIRED_MISSING, module.status());
    }

    @Test
    void packDisabledOpcionalEhObservavelSemMutacao() {
        PaperDatapackContractModule module = module(
                new PaperDatapackContractSettings(
                        true,
                        "file/living-world",
                        false
                ),
                () -> Optional.of(manifest()),
                new AtomicInteger(),
                pack(false)
        );

        module.enable();

        assertEquals(
                DatapackRuntimeStatus.COMPATIBLE_DISABLED,
                module.status()
        );
    }

    private CompanionPackManifest manifest() {
        return CompanionPackManifest.withoutHash(
                CompanionPackKind.DATAPACK,
                "1.0.0",
                1
        );
    }

    private PaperDatapackContractModule module(
            PaperDatapackContractSettings settings,
            dev.signalshards.livingworld.core.companion.CompanionPackManifestSource source,
            AtomicInteger managerReads,
            Datapack pack
    ) {
        DatapackManager manager = (DatapackManager) Proxy.newProxyInstance(
                DatapackManager.class.getClassLoader(),
                new Class<?>[]{DatapackManager.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getPack")) {
                        managerReads.incrementAndGet();
                        return pack;
                    }
                    throw new AssertionError(
                            "Chamada inesperada: " + method.getName()
                    );
                }
        );
        Server server = (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getDatapackManager" -> manager;
                    default -> null;
                }
        );
        Plugin plugin = (Plugin) Proxy.newProxyInstance(
                Plugin.class.getClassLoader(),
                new Class<?>[]{Plugin.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getServer" -> server;
                    case "getLogger" -> Logger.getLogger(
                            "PaperDatapackContractModuleTest"
                    );
                    default -> null;
                }
        );
        return new PaperDatapackContractModule(plugin, settings, source);
    }

    private Datapack pack(boolean enabled) {
        return (Datapack) Proxy.newProxyInstance(
                Datapack.class.getClassLoader(),
                new Class<?>[]{Datapack.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isEnabled" -> enabled;
                    default -> throw new AssertionError(
                            "Chamada inesperada: " + method.getName()
                    );
                }
        );
    }
}
