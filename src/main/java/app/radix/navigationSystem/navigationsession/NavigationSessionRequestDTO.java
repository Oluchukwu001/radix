package app.radix.navigationSystem.navigationsession;



public record NavigationSessionRequestDTO(

        Long startNodeId,

        Long destinationNodeId,

        String deviceFingerprint

) {
}
