package app.radix.navigationSystem.Location;





import java.util.Set;

public record LocationNodeRequestDTO(

        String nodeCode,

        String name,

        String displayName,

        String description,

        LocationType locationType,

        Boolean destinationAllowed,

        Boolean scanOriginAllowed,

        Boolean navigableNode,

        Long campusId,

        Long parentId,

        Double latitude,

        Double longitude,

        Double altitude,

        Integer floorLevel,

        Set<String> aliases

) {
}