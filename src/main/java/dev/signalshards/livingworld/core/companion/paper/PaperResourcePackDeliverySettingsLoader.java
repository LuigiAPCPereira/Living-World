package dev.signalshards.livingworld.core.companion.paper;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public final class PaperResourcePackDeliverySettingsLoader {
    private PaperResourcePackDeliverySettingsLoader() {
    }

    public static PaperResourcePackDeliverySettings load(
            ConfigurationSection config
    ) {
        Objects.requireNonNull(config, "configuração");
        PaperResourcePackDeliverySettings defaults =
                PaperResourcePackDeliverySettings.disabled();
        return new PaperResourcePackDeliverySettings(
                config.getBoolean(
                        "companions.resource-pack.enabled",
                        defaults.enabled()
                ),
                config.getString(
                        "companions.resource-pack.url",
                        defaults.url()
                ),
                config.getString(
                        "companions.resource-pack.sha1",
                        defaults.sha1()
                ),
                config.getBoolean(
                        "companions.resource-pack.required",
                        defaults.required()
                ),
                config.getString(
                        "companions.resource-pack.prompt",
                        defaults.prompt()
                )
        );
    }
}
