package app.radix.navigationSystem.navigationsession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.radix.navigationSystem.Location.LocationNode;
import app.radix.navigationSystem.Location.LocationNodeRepository;
import app.radix.navigationSystem.exceptions.ConflictException;
import app.radix.navigationSystem.exceptions.NotFoundException;
import app.radix.navigationSystem.exceptions.ValidationException;
import app.radix.navigationSystem.utils.ServiceUtil;
import app.radix.navigationSystem.utils.ValidationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NavigationSessionService {

    private static final String ENTITY_NAME = "NavigationSession";
    private static final String NODE_NAME = "LocationNode";

    private final NavigationSessionRepository sessionRepository;
    private final LocationNodeRepository locationNodeRepository;

    // -------------------------------------------------------------------------
    // START (create)
    // -------------------------------------------------------------------------

    /**
     * Starts a new navigation session.
     * Generates a unique session token, sets status ACTIVE, stamps startedAt,
     * and initialises currentNode to the start node.
     */
    @Transactional
    public NavigationSession start(NavigationSessionRequestDTO request) {
        ValidationUtil.requireNonNull(request, "Request body is required");

        ValidationUtil.requireNonNull(request.startNodeId(), "Start node ID is required");
        ValidationUtil.requirePositive(request.startNodeId(), "Start node ID must be a positive value");

        ValidationUtil.requireNonNull(request.destinationNodeId(), "Destination node ID is required");
        ValidationUtil.requirePositive(request.destinationNodeId(), "Destination node ID must be a positive value");

        if (request.startNodeId().equals(request.destinationNodeId())) {
            throw new ConflictException("Start and destination nodes must be different");
        }

        LocationNode startNode = ServiceUtil.findOrThrow(
                locationNodeRepository, request.startNodeId(), NODE_NAME + " (start)");
        LocationNode destinationNode = ServiceUtil.findOrThrow(
                locationNodeRepository, request.destinationNodeId(), NODE_NAME + " (destination)");

        // Business rules: origin must be scannable, destination must be allowed.
        if (!startNode.isScanOriginAllowed()) {
            throw new ValidationException("Start node is not a valid scan origin");
        }
        if (!destinationNode.isDestinationAllowed()) {
            throw new ValidationException("Destination node is not a valid destination");
        }

        NavigationSession session = new NavigationSession();
        session.setSessionToken(generateUniqueToken());
        session.setStartNode(startNode);
        session.setDestinationNode(destinationNode);
        session.setCurrentNode(startNode);
        session.setStatus(NavigationStatus.ACTIVE);
        session.setDeviceFingerprint(request.deviceFingerprint());
        session.setStartedAt(LocalDateTime.now());

        return sessionRepository.save(session);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public NavigationSession getById(Long id) {
        return ServiceUtil.findOrThrow(sessionRepository, id, ENTITY_NAME);
    }

    @Transactional(readOnly = true)
    public NavigationSession getByToken(UUID token) {
        ValidationUtil.requireNonNull(token, "Session token is required");
        return sessionRepository.findBySessionToken(token)
                .orElseThrow(() -> new NotFoundException("Navigation session not found for the given token"));
    }

    @Transactional(readOnly = true)
    public Page<NavigationSession> getAll(int page, int size, String sortBy) {
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "startedAt" : sortBy;
        return ServiceUtil.findAllPaged(sessionRepository, page, size, safeSortBy);
    }

    @Transactional(readOnly = true)
    public List<NavigationSession> getByStatus(NavigationStatus status) {
        ValidationUtil.requireNonNull(status, "Status is required");
        return sessionRepository.findByStatus(status);
    }

    // -------------------------------------------------------------------------
    // LIFECYCLE TRANSITIONS
    // -------------------------------------------------------------------------

    /**
     * Advances the session to a new current node. Only allowed while ACTIVE.
     */
    @Transactional
    public NavigationSession advance(Long id, AdvanceRequestDTO request) {
        ValidationUtil.requireNonNull(request, "Request body is required");
        ValidationUtil.requireNonNull(request.currentNodeId(), "Current node ID is required");
        ValidationUtil.requirePositive(request.currentNodeId(), "Current node ID must be a positive value");

        NavigationSession session = ServiceUtil.findOrThrow(sessionRepository, id, ENTITY_NAME);
        requireStatus(session, NavigationStatus.ACTIVE, "advance");

        LocationNode currentNode = ServiceUtil.findOrThrow(
                locationNodeRepository, request.currentNodeId(), NODE_NAME + " (current)");
        session.setCurrentNode(currentNode);

        // Reaching the destination auto-completes the session.
        if (currentNode.getId().equals(session.getDestinationNode().getId())) {
            session.setStatus(NavigationStatus.COMPLETED);
            session.setCompletedAt(LocalDateTime.now());
        }

        return sessionRepository.save(session);
    }

    /**
     * Pauses an ACTIVE session.
     */
    @Transactional
    public NavigationSession pause(Long id) {
        NavigationSession session = ServiceUtil.findOrThrow(sessionRepository, id, ENTITY_NAME);
        requireStatus(session, NavigationStatus.ACTIVE, "pause");
        session.setStatus(NavigationStatus.PAUSED);
        return sessionRepository.save(session);
    }

    /**
     * Resumes a PAUSED session.
     */
    @Transactional
    public NavigationSession resume(Long id) {
        NavigationSession session = ServiceUtil.findOrThrow(sessionRepository, id, ENTITY_NAME);
        requireStatus(session, NavigationStatus.PAUSED, "resume");
        session.setStatus(NavigationStatus.ACTIVE);
        return sessionRepository.save(session);
    }

    /**
     * Reroutes a session, optionally to a new current node and/or destination.
     * Allowed while ACTIVE or PAUSED; leaves the session ACTIVE afterward.
     */
    @Transactional
    public NavigationSession reroute(Long id, RerouteRequestDTO request) {
        ValidationUtil.requireNonNull(request, "Request body is required");

        NavigationSession session = ServiceUtil.findOrThrow(sessionRepository, id, ENTITY_NAME);
        if (isTerminal(session.getStatus())) {
            throw new ConflictException(
                    "Cannot reroute a session that is " + session.getStatus());
        }

        ServiceUtil.setIfPresent(
                request.currentNodeId(),
                nodeId -> session.setCurrentNode(
                        ServiceUtil.findOrThrow(locationNodeRepository, nodeId, NODE_NAME + " (current)")),
                nodeId -> ValidationUtil.requirePositive(nodeId, "Current node ID must be a positive value"));

        ServiceUtil.setIfPresent(
                request.destinationNodeId(),
                nodeId -> {
                    LocationNode dest = ServiceUtil.findOrThrow(
                            locationNodeRepository, nodeId, NODE_NAME + " (destination)");
                    if (!dest.isDestinationAllowed()) {
                        throw new ValidationException("Destination node is not a valid destination");
                    }
                    session.setDestinationNode(dest);
                },
                nodeId -> ValidationUtil.requirePositive(nodeId, "Destination node ID must be a positive value"));

        session.setStatus(NavigationStatus.REROUTED);
        NavigationSession saved = sessionRepository.save(session);

        // A rerouted session immediately continues navigating.
        saved.setStatus(NavigationStatus.ACTIVE);
        return sessionRepository.save(saved);
    }

    /**
     * Marks a session COMPLETED. Allowed from ACTIVE or PAUSED.
     */
    @Transactional
    public NavigationSession complete(Long id) {
        NavigationSession session = ServiceUtil.findOrThrow(sessionRepository, id, ENTITY_NAME);
        if (isTerminal(session.getStatus())) {
            throw new ConflictException(
                    "Cannot complete a session that is already " + session.getStatus());
        }
        session.setStatus(NavigationStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    /**
     * Marks a session CANCELLED. Allowed from any non-terminal state.
     */
    @Transactional
    public NavigationSession cancel(Long id) {
        NavigationSession session = ServiceUtil.findOrThrow(sessionRepository, id, ENTITY_NAME);
        if (isTerminal(session.getStatus())) {
            throw new ConflictException(
                    "Cannot cancel a session that is already " + session.getStatus());
        }
        session.setStatus(NavigationStatus.CANCELLED);
        session.setCompletedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    // -------------------------------------------------------------------------
    // DELETE (hard)
    // -------------------------------------------------------------------------

    @Transactional
    public void delete(Long id) {
        ServiceUtil.assertExists(sessionRepository, id, ENTITY_NAME);
        sessionRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private UUID generateUniqueToken() {
        UUID token = UUID.randomUUID();
        // Astronomically unlikely to collide, but guard anyway.
        while (sessionRepository.existsBySessionToken(token)) {
            token = UUID.randomUUID();
        }
        return token;
    }

    private void requireStatus(NavigationSession session, NavigationStatus expected, String action) {
        if (session.getStatus() != expected) {
            throw new ConflictException(
                    "Cannot " + action + " a session in status " + session.getStatus()
                            + "; expected " + expected);
        }
    }

    private boolean isTerminal(NavigationStatus status) {
        return status == NavigationStatus.COMPLETED || status == NavigationStatus.CANCELLED;
    }
}
