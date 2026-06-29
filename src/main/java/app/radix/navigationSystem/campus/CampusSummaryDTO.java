package app.radix.navigationSystem.campus;

/**
 * Lightweight Campus view embedded inside other entities' responses
 * (e.g. LocationNode). Avoids serializing the full campus graph.
 */
public record CampusSummaryDTO(
        Long id,
        String campusCode,
        String name
) {
}
