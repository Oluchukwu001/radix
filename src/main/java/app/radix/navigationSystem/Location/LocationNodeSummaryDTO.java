package app.radix.navigationSystem.Location;

/**
 * Lightweight LocationNode view used for the nested parent reference,
 * preventing infinite recursion through the parent/children graph.
 */
public record LocationNodeSummaryDTO(
        Long id,
        String nodeCode,
        String name,
        LocationType locationType
) {
}
