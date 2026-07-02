package app.radix.navigationSystem.navigationsession;

/**
 * Payload for advancing a session's progress to a new current node.
 */
public record AdvanceRequestDTO(
        Long currentNodeId
) {
}
