package dev.signalshards.livingworld.features.desirelines.domain;

import java.util.Objects;

public final class PathWearPolicy {
    private final PathWearSettings settings;

    public PathWearPolicy(PathWearSettings settings) {
        this.settings = Objects.requireNonNull(settings, "configuração de desgaste");
    }

    public PathWearStage stageFor(int visits) {
        if (visits < 0) {
            throw new IllegalArgumentException("A quantidade de passagens não pode ser negativa");
        }
        if (visits >= settings.dirtToPathVisits()) {
            return PathWearStage.CAMINHO;
        }
        if (visits >= settings.grassToDirtVisits()) {
            return PathWearStage.DESGASTADO;
        }
        return PathWearStage.NATURAL;
    }
}
