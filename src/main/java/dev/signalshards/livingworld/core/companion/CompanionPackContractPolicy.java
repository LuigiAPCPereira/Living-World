package dev.signalshards.livingworld.core.companion;

import java.util.Locale;
import java.util.Objects;

/**
 * Verifica compatibilidade por contrato/schema, não por versão textual.
 */
public final class CompanionPackContractPolicy {
    public CompanionPackCompatibility evaluateMissing(
            CompanionPackRequirement requirement
    ) {
        Objects.requireNonNull(requirement, "requisito");
        return requirement.required()
                ? CompanionPackCompatibility.REQUIRED_MISSING
                : CompanionPackCompatibility.OPTIONAL_MISSING;
    }

    public CompanionPackCompatibility evaluate(
            CompanionPackRequirement requirement,
            CompanionPackManifest candidate
    ) {
        Objects.requireNonNull(requirement, "requisito");
        Objects.requireNonNull(candidate, "manifest");
        if (candidate.kind() != requirement.kind()) {
            return CompanionPackCompatibility.WRONG_KIND;
        }
        if (candidate.schema() != requirement.schema()) {
            return CompanionPackCompatibility.SCHEMA_MISMATCH;
        }
        return CompanionPackCompatibility.COMPATIBLE;
    }

    public CompanionPackCompatibility evaluate(
            CompanionPackRequirement requirement,
            CompanionPackManifest candidate,
            String observedSha256
    ) {
        CompanionPackCompatibility contract = evaluate(requirement, candidate);
        if (contract != CompanionPackCompatibility.COMPATIBLE) {
            return contract;
        }
        Objects.requireNonNull(observedSha256, "hash observado");
        if (candidate.sha256().isPresent()
                && !candidate.sha256().orElseThrow().equals(
                observedSha256.toLowerCase(Locale.ROOT)
        )) {
            return CompanionPackCompatibility.HASH_MISMATCH;
        }
        return CompanionPackCompatibility.COMPATIBLE;
    }
}
