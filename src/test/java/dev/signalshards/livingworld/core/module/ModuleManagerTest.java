package dev.signalshards.livingworld.core.module;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModuleManagerTest {
    @Test
    void disablesModulesInReverseOrder() {
        List<String> events = new ArrayList<>();
        ModuleManager manager = new ModuleManager(
                module("first", events),
                module("second", events)
        );

        manager.enableAll();
        manager.disableAll();

        assertEquals(List.of(
                "enable:first",
                "enable:second",
                "disable:second",
                "disable:first"
        ), events);
    }

    @Test
    void rollsBackEnabledModulesWhenStartupFails() {
        List<String> events = new ArrayList<>();
        LivingWorldModule first = module("first", events);
        LivingWorldModule failing = new LivingWorldModule() {
            @Override
            public void enable() {
                events.add("enable:failing");
                throw new IllegalStateException("falha");
            }

            @Override
            public void disable() {
                events.add("disable:failing");
            }
        };

        ModuleManager manager = new ModuleManager(first, failing);

        assertThrows(IllegalStateException.class, manager::enableAll);
        assertEquals(List.of(
                "enable:first",
                "enable:failing",
                "disable:first"
        ), events);
    }

    private static LivingWorldModule module(String id, List<String> events) {
        return new LivingWorldModule() {
            @Override
            public void enable() {
                events.add("enable:" + id);
            }

            @Override
            public void disable() {
                events.add("disable:" + id);
            }
        };
    }
}
