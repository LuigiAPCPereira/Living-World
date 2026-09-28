package dev.signalshards.livingworld.core.companion.paper;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public final class PaperDatapackContractSettingsLoader {
    private PaperDatapackContractSettingsLoader() {
    }

    public static PaperDatapackContractSettings load(
            ConfigurationSection config
    ) {
        Objects.requireNonNull(config, "configuração");
        PaperDatapackContractSettings defaults =
                PaperDatapackContractSettings.disabled();
        return new PaperDatapackContractSettings(
                config.getBoolean(
                        "companions.datapack.enabled",
                        defaults.enabled()
                ),
                config.getString(
                        "companions.datapack.paper-name",
                        defaults.paperName()
                ),
                config.getBoolean(
                        "companions.datapack.required",
                        defaults.required()
                )
        );
    }
}
