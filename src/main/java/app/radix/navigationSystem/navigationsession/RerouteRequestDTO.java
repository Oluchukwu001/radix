package app.radix.navigationSystem.navigationsession;

/**
 * Payload for rerouting a session, optionally to a new destination and/or
 * from a new current node.
 */
public record RerouteRequestDTO(
        Long currentNodeId,
        Long destinationNodeId
) {
}
