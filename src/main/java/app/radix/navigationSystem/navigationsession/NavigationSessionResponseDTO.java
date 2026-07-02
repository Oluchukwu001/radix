package app.radix.navigationSystem.navigationsession;

import java.time.LocalDateTime;
import java.util.UUID;

import app.radix.navigationSystem.Location.LocationNodeSummaryDTO;

/**
 * Full NavigationSession response. The three node relations are exposed as
 * lightweight LocationNode summaries to keep the payload flat and avoid
 * serializing the full node graph (parent/children/campus).
 */
public record NavigationSessionResponseDTO(
        Long id,
        UUID sessionToken,
        LocationNodeSummaryDTO startNode,
        LocationNodeSummaryDTO destinationNode,
        LocationNodeSummaryDTO currentNode,
        NavigationStatus status,
        Double totalDistanceMeters,
        Integer estimatedDurationSeconds,
        String deviceFingerprint,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
}
