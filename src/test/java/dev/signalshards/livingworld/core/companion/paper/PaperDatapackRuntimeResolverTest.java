package dev.signalshards.livingworld.core.companion.paper;

import dev.signalshards.livingworld.core.companion.CompanionPackCompatibility;
import dev.signalshards.livingworld.core.companion.DatapackRuntimeStatus;
import io.papermc.paper.datapack.Datapack;
import io.papermc.paper.datapack.DatapackManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperDatapackRuntimeResolverTest {
    private final PaperDatapackRuntimeResolver resolver =
            new PaperDatapackRuntimeResolver();

    @Test
    void compatibleUsaEstadoEnabledDoPaper() {
        assertEquals(
                DatapackRuntimeStatus.COMPATIBLE_ENABLED,
                resolver.resolve(
                        manager(pack(true)),
                        "file/living-world",
                        CompanionPackCompatibility.COMPATIBLE
                )
        );
        assertEquals(
                DatapackRuntimeStatus.COMPATIBLE_DISABLED,
                resolver.resolve(
                        manager(pack(false)),
                        "file/living-world",
                        CompanionPackCompatibility.COMPATIBLE
                )
        );
    }

    @Test
    void packNaoDescobertoResultaEmMissingSemRefreshAutomatico() {
        assertEquals(
                DatapackRuntimeStatus.OPTIONAL_MISSING,
                resolver.resolve(
                        manager(null),
                        "file/living-world",
                        CompanionPackCompatibility.COMPATIBLE
                )
        );
    }

    @Test
    void manifestIncompativelNemPrecisaConsultarPack() {
        DatapackManager manager = (DatapackManager) Proxy.newProxyInstance(
                DatapackManager.class.getClassLoader(),
                new Class<?>[]{DatapackManager.class},
                (proxy, method, args) -> {
                    throw new AssertionError("DatapackManager não deveria ser consultado");
                }
        );

        assertEquals(
                DatapackRuntimeStatus.INCOMPATIBLE,
                resolver.resolve(
                        manager,
                        "file/living-world",
                        CompanionPackCompatibility.SCHEMA_MISMATCH
                )
        );
    }

    private DatapackManager manager(Datapack pack) {
        return (DatapackManager) Proxy.newProxyInstance(
                DatapackManager.class.getClassLoader(),
                new Class<?>[]{DatapackManager.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPack" -> pack;
                    default -> throw new AssertionError(
                            "Chamada inesperada: " + method.getName()
                    );
                }
        );
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
