package app.radix.navigationSystem.campus;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CampusMapper {

    CampusResponseDTO toResponse(Campus campus);

    CampusSummaryDTO toSummary(Campus campus);

    List<CampusResponseDTO> toResponseList(List<Campus> campuses);

    /**
     * Request -> new entity. The id and JPA-managed relations (locationNodes)
     * are not part of the request, so they are left untouched.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "locationNodes", ignore = true)
    Campus toEntity(CampusRequestDTO request);

    /**
     * Applies non-null request fields onto an existing entity (partial update).
     * Null properties in the request are ignored thanks to the strategy above.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "locationNodes", ignore = true)
    void updateEntity(CampusRequestDTO request, @MappingTarget Campus campus);
}
