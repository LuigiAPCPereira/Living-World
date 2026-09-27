package dev.signalshards.livingworld.core.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ModuleManager {
    private final List<LivingWorldModule> modules;
    private final List<LivingWorldModule> enabledModules = new ArrayList<>();

    public ModuleManager(LivingWorldModule... modules) {
        this.modules = List.of(modules);
    }

    public void enableAll() {
        if (!enabledModules.isEmpty()) {
            throw new IllegalStateException("Os módulos já estão habilitados");
        }

        try {
            for (LivingWorldModule module : modules) {
                LivingWorldModule nonNullModule = Objects.requireNonNull(module, "módulo");
                nonNullModule.enable();
                enabledModules.add(nonNullModule);
            }
        } catch (RuntimeException exception) {
            disableAll();
            throw exception;
        }
    }

    public void disableAll() {
        RuntimeException firstFailure = null;

        for (int index = enabledModules.size() - 1; index >= 0; index--) {
            LivingWorldModule module = enabledModules.get(index);
            try {
                module.disable();
            } catch (RuntimeException exception) {
                if (firstFailure == null) {
                    firstFailure = exception;
                } else {
                    firstFailure.addSuppressed(exception);
                }
            }
        }

        enabledModules.clear();

        if (firstFailure != null) {
            throw firstFailure;
        }
    }
}
