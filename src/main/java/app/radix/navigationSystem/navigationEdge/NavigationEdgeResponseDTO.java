package app.radix.navigationSystem.navigationEdge;

import app.radix.navigationSystem.Location.LocationNodeSummaryDTO;

/**
 * Full NavigationEdge response. The two endpoints are exposed as lightweight
 * LocationNode summaries so the response carries node identity without
 * serializing the full node graph (parent/children/campus).
 */
public record NavigationEdgeResponseDTO(
        Long id,
        LocationNodeSummaryDTO fromNode,
        LocationNodeSummaryDTO toNode,
        Double distanceMeters,
        Integer estimatedWalkTimeSeconds,
        PathType pathType,
        boolean bidirectional,
        boolean accessible,
        boolean active
) {
}
