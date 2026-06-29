package app.radix.navigationSystem.Location;

import java.util.Set;

import app.radix.navigationSystem.campus.CampusSummaryDTO;

/**
 * Full LocationNode response. Relations are exposed as nested summaries:
 * - campus  -> CampusSummaryDTO
 * - parent  -> LocationNodeSummaryDTO (children are intentionally omitted to
 *   avoid serializing the whole subtree)
 */
public record LocationNodeResponseDTO(
        Long id,
        String nodeCode,
        String name,
        String displayName,
        String description,
        LocationType locationType,
        boolean destinationAllowed,
        boolean scanOriginAllowed,
        boolean navigableNode,
        CampusSummaryDTO campus,
        LocationNodeSummaryDTO parent,
        Double latitude,
        Double longitude,
        Double altitude,
        Integer floorLevel,
        Set<String> aliases
) {
}
