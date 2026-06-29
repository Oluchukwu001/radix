package app.radix.navigationSystem.navigationEdge;

import java.util.List;

import org.mapstruct.Mapper;

import app.radix.navigationSystem.Location.LocationNodeMapper;

/**
 * Maps NavigationEdge entities to response DTOs.
 * Reuses LocationNodeMapper (via `uses`) to turn fromNode/toNode into
 * LocationNodeSummaryDTO, which breaks the entity graph and prevents recursion.
 */
@Mapper(
        componentModel = "spring",
        uses = { LocationNodeMapper.class })
public interface NavigationEdgeMapper {

    NavigationEdgeResponseDTO toResponse(NavigationEdge edge);

    List<NavigationEdgeResponseDTO> toResponseList(List<NavigationEdge> edges);
}
