package app.radix.navigationSystem.navigationEdge;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.radix.navigationSystem.Location.LocationNode;
import app.radix.navigationSystem.Location.LocationNodeRepository;
import app.radix.navigationSystem.exceptions.ConflictException;
import app.radix.navigationSystem.utils.ServiceUtil;
import app.radix.navigationSystem.utils.ValidationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NavigationEdgeService {

    private static final String ENTITY_NAME = "NavigationEdge";
    private static final String NODE_NAME = "LocationNode";

    private final NavigationEdgeRepository navigationEdgeRepository;
    private final LocationNodeRepository locationNodeRepository;

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Transactional
    public NavigationEdge create(NavigationEdgeRequestDTO request) {
        validate(request);

        LocationNode fromNode = ServiceUtil.findOrThrow(
                locationNodeRepository, request.fromNodeId(), NODE_NAME + " (from)");
        LocationNode toNode = ServiceUtil.findOrThrow(
                locationNodeRepository, request.toNodeId(), NODE_NAME + " (to)");

        assertPairUnique(request.fromNodeId(), request.toNodeId());

        NavigationEdge edge = toEntity(request, fromNode, toNode);
        return navigationEdgeRepository.save(edge);
    }

    // -------------------------------------------------------------------------
    // BATCH CREATE (all-or-nothing)
    // -------------------------------------------------------------------------

    /**
     * Creates many navigation edges in a single transaction.
     * All-or-nothing: if ANY item fails validation, references a missing node,
     * or duplicates a (from, to) pair (against the DB or another item in the
     * same batch), the entire batch is rolled back and nothing is saved.
     */
    @Transactional
    public List<NavigationEdge> createBatch(List<NavigationEdgeRequestDTO> requests) {
        ValidationUtil.requireNotEmpty(requests, "Batch must contain at least one navigation edge");

        Set<String> seenPairs = new HashSet<>();
        List<NavigationEdge> toSave = new ArrayList<>(requests.size());

        for (int i = 0; i < requests.size(); i++) {
            NavigationEdgeRequestDTO request = requests.get(i);
            String position = "Item " + (i + 1) + ": ";

            ValidationUtil.requireNonNull(request, position + "request body is required");
            validate(request);

            String pairKey = request.fromNodeId() + "->" + request.toNodeId();
            if (!seenPairs.add(pairKey)) {
                throw new ConflictException(
                        position + "duplicate edge " + pairKey + " within the batch");
            }

            LocationNode fromNode = ServiceUtil.findOrThrow(
                    locationNodeRepository, request.fromNodeId(), NODE_NAME + " (from)");
            LocationNode toNode = ServiceUtil.findOrThrow(
                    locationNodeRepository, request.toNodeId(), NODE_NAME + " (to)");

            assertPairUnique(request.fromNodeId(), request.toNodeId());

            toSave.add(toEntity(request, fromNode, toNode));
        }

        return navigationEdgeRepository.saveAll(toSave);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public NavigationEdge getById(Long id) {
        return ServiceUtil.findOrThrow(navigationEdgeRepository, id, ENTITY_NAME);
    }

    @Transactional(readOnly = true)
    public Page<NavigationEdge> getAll(int page, int size, String sortBy) {
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        return ServiceUtil.findAllPaged(navigationEdgeRepository, page, size, safeSortBy);
    }

    @Transactional(readOnly = true)
    public List<NavigationEdge> getOutgoing(Long fromNodeId) {
        ValidationUtil.requirePositive(fromNodeId, "From node ID must be a positive value");
        ServiceUtil.assertExists(locationNodeRepository, fromNodeId, NODE_NAME);
        return navigationEdgeRepository.findByFromNodeId(fromNodeId);
    }

    @Transactional(readOnly = true)
    public List<NavigationEdge> getIncoming(Long toNodeId) {
        ValidationUtil.requirePositive(toNodeId, "To node ID must be a positive value");
        ServiceUtil.assertExists(locationNodeRepository, toNodeId, NODE_NAME);
        return navigationEdgeRepository.findByToNodeId(toNodeId);
    }

    // -------------------------------------------------------------------------
    // UPDATE (partial)
    // -------------------------------------------------------------------------

    @Transactional
    public NavigationEdge update(Long id, NavigationEdgeRequestDTO request) {
        ValidationUtil.requireNonNull(request, "Request body is required");

        NavigationEdge edge = ServiceUtil.findOrThrow(navigationEdgeRepository, id, ENTITY_NAME);

        // Determine the resulting endpoint IDs so we can re-check the unique pair.
        Long newFromId = request.fromNodeId() != null
                ? request.fromNodeId() : edge.getFromNode().getId();
        Long newToId = request.toNodeId() != null
                ? request.toNodeId() : edge.getToNode().getId();

        ServiceUtil.setIfPresent(
                request.fromNodeId(),
                fromId -> edge.setFromNode(
                        ServiceUtil.findOrThrow(locationNodeRepository, fromId, NODE_NAME + " (from)")),
                fromId -> ValidationUtil.requirePositive(fromId, "From node ID must be a positive value"));

        ServiceUtil.setIfPresent(
                request.toNodeId(),
                toId -> edge.setToNode(
                        ServiceUtil.findOrThrow(locationNodeRepository, toId, NODE_NAME + " (to)")),
                toId -> ValidationUtil.requirePositive(toId, "To node ID must be a positive value"));

        // If either endpoint changed, ensure the new pair isn't taken by another edge.
        if (request.fromNodeId() != null || request.toNodeId() != null) {
            navigationEdgeRepository.findByFromNodeId(newFromId).stream()
                    .filter(e -> e.getToNode().getId().equals(newToId))
                    .filter(e -> !e.getId().equals(id))
                    .findAny()
                    .ifPresent(e -> {
                        throw new ConflictException(
                                "An edge from node " + newFromId + " to node " + newToId + " already exists");
                    });
        }

        ServiceUtil.setIfPresent(
                request.distanceMeters(),
                edge::setDistanceMeters,
                value -> ValidationUtil.requirePositive(value, "Distance must be greater than zero"));

        ServiceUtil.setIfPresent(
                request.estimatedWalkTimeSeconds(),
                edge::setEstimatedWalkTimeSeconds,
                value -> ValidationUtil.requirePositive(value, "Estimated walk time must be greater than zero"));

        ServiceUtil.setIfPresent(request.pathType(), edge::setPathType);
        ServiceUtil.setIfPresent(request.bidirectional(), edge::setBidirectional);
        ServiceUtil.setIfPresent(request.accessible(), edge::setAccessible);
        ServiceUtil.setIfPresent(request.active(), edge::setActive);

        return navigationEdgeRepository.save(edge);
    }

    // -------------------------------------------------------------------------
    // DELETE (hard)
    // -------------------------------------------------------------------------

    @Transactional
    public void delete(Long id) {
        ServiceUtil.assertExists(navigationEdgeRepository, id, ENTITY_NAME);
        navigationEdgeRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private void assertPairUnique(Long fromNodeId, Long toNodeId) {
        if (navigationEdgeRepository.existsByFromNodeIdAndToNodeId(fromNodeId, toNodeId)) {
            throw new ConflictException(
                    "An edge from node " + fromNodeId + " to node " + toNodeId + " already exists");
        }
    }

    private NavigationEdge toEntity(NavigationEdgeRequestDTO request, LocationNode fromNode, LocationNode toNode) {
        NavigationEdge edge = new NavigationEdge();
        edge.setFromNode(fromNode);
        edge.setToNode(toNode);
        edge.setDistanceMeters(request.distanceMeters());
        edge.setEstimatedWalkTimeSeconds(request.estimatedWalkTimeSeconds());
        edge.setPathType(request.pathType());
        edge.setBidirectional(request.bidirectional() == null || request.bidirectional());
        edge.setAccessible(request.accessible() == null || request.accessible());
        edge.setActive(request.active() == null || request.active());
        return edge;
    }

    private void validate(NavigationEdgeRequestDTO request) {
        ValidationUtil.requireNonNull(request, "Request body is required");

        ValidationUtil.requireNonNull(request.fromNodeId(), "From node ID is required");
        ValidationUtil.requirePositive(request.fromNodeId(), "From node ID must be a positive value");

        ValidationUtil.requireNonNull(request.toNodeId(), "To node ID is required");
        ValidationUtil.requirePositive(request.toNodeId(), "To node ID must be a positive value");

        if (request.fromNodeId().equals(request.toNodeId())) {
            throw new ConflictException("An edge cannot start and end at the same node");
        }

        ValidationUtil.requirePositive(request.distanceMeters(), "Distance is required and must be greater than zero");
        ValidationUtil.requirePositive(request.estimatedWalkTimeSeconds(),
                "Estimated walk time is required and must be greater than zero");

        ValidationUtil.requireNonNull(request.pathType(), "Path type is required");
    }
}

