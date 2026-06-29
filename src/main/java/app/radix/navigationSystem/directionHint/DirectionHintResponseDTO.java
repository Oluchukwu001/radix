package app.radix.navigationSystem.directionHint;

/**
 * The related NavigationEdge is exposed as a small nested summary so the
 * response carries edge identity without serializing the full edge graph.
 */
public record DirectionHintResponseDTO(
        Long id,
        EdgeSummary edge,
        DirectionEnum direction,
        String hintText,
        String landmarkReference,
        Integer priority
) {

    public record EdgeSummary(
            Long id
    ) {
    }
}
