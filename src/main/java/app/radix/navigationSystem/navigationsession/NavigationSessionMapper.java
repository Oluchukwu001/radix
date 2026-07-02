package app.radix.navigationSystem.navigationsession;

import java.util.List;

import org.mapstruct.Mapper;

import app.radix.navigationSystem.Location.LocationNodeMapper;

/**
 * Maps NavigationSession entities to response DTOs.
 * Reuses LocationNodeMapper (via `uses`) to turn startNode/destinationNode/
 * currentNode into LocationNodeSummaryDTO, breaking the entity graph so the
 * response never recurses.
 */
@Mapper(
        componentModel = "spring",
        uses = { LocationNodeMapper.class })
public interface NavigationSessionMapper {

    NavigationSessionResponseDTO toResponse(NavigationSession session);

    List<NavigationSessionResponseDTO> toResponseList(List<NavigationSession> sessions);
}
