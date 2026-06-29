package app.radix.navigationSystem.navigationEdge;



public record NavigationEdgeRequestDTO(

        Long fromNodeId,

        Long toNodeId,

        Double distanceMeters,

        Integer estimatedWalkTimeSeconds,

        PathType pathType,

        Boolean bidirectional,

        Boolean accessible,

        Boolean active

) {
}