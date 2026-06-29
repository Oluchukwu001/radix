package app.radix.navigationSystem.Location;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import app.radix.navigationSystem.campus.CampusMapper;

@Mapper(componentModel = "spring", uses = {
        CampusMapper.class }, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LocationNodeMapper {

    /**
     * campus -> CampusSummaryDTO is delegated to CampusMapper (via `uses`).
     * parent -> LocationNodeSummaryDTO via toSummary below.
     */
    LocationNodeResponseDTO toResponse(LocationNode node);

    List<LocationNodeResponseDTO> toResponseList(List<LocationNode> nodes);

    LocationNodeSummaryDTO toSummary(LocationNode node);

    /**
     * Request -> new entity. Relations (campus, parent) and the children
     * collection are resolved/managed by the service, so they are ignored here.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "campus", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    LocationNode toEntity(LocationNodeRequestDTO request);

    /**
     * Partial update of scalar fields only. campus and parent require repository
     * lookups and are handled in the service; id and children are never touched.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "campus", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    void updateEntity(LocationNodeRequestDTO request, @MappingTarget LocationNode node);
}
