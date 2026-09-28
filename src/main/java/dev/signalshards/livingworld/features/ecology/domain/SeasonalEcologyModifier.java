package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.seasons.domain.Season;

/**
 * Aplica a influência da estação sobre processos ecológicos.
 *
 * <p>Esta camada não conhece mundo, blocos ou jogadores. Ela apenas traduz a
 * estação atual em multiplicadores de comportamento.</p>
 */
public interface SeasonalEcologyModifier {

    double cropMultiplier(Season season);

    double treeMultiplier(Season season);

    double grassMultiplier(Season season);

    default double multiplier(GrowthCategory category, Season season) {
        return switch (category) {
            case CROP -> cropMultiplier(season);
            case TREE -> treeMultiplier(season);
            case GROUND_COVER -> grassMultiplier(season);
        };
    }
}
