package app.radix.navigationSystem.directionHint;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import app.radix.navigationSystem.navigationEdge.NavigationEdge;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DirectionHintMapper {

    DirectionHintResponseDTO toResponse(DirectionHint hint);

    List<DirectionHintResponseDTO> toResponseList(List<DirectionHint> hints);

    /**
     * Maps the related edge entity to the small nested EdgeSummary.
     * Returns null when the hint has no edge.
     */
    default DirectionHintResponseDTO.EdgeSummary toEdgeSummary(NavigationEdge edge) {
        return edge == null ? null : new DirectionHintResponseDTO.EdgeSummary(edge.getId());
    }

    /**
     * Request -> new entity. The edge is resolved by the service from edgeId,
     * so it is ignored here along with the id.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "edge", ignore = true)
    DirectionHint toEntity(DirectionHintRequestDTO request);

    /**
     * Partial update of scalar fields. The edge (relation) is handled in the
     * service because it requires a repository lookup; id is never overwritten.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "edge", ignore = true)
    void updateEntity(DirectionHintRequestDTO request, @MappingTarget DirectionHint hint);
}
