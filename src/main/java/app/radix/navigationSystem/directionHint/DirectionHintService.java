package app.radix.navigationSystem.directionHint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.radix.navigationSystem.exceptions.ConflictException;
import app.radix.navigationSystem.navigationEdge.NavigationEdge;
import app.radix.navigationSystem.navigationEdge.NavigationEdgeRepository;
import app.radix.navigationSystem.utils.ServiceUtil;
import app.radix.navigationSystem.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectionHintService {

    private static final String ENTITY_NAME = "DirectionHint";
    private static final String EDGE_NAME = "NavigationEdge";
    private static final int MAX_HINT_LENGTH = 500;
    private static final int MAX_LANDMARK_LENGTH = 255;

    private final DirectionHintRepository directionHintRepository;
    private final NavigationEdgeRepository navigationEdgeRepository;

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Transactional
    public DirectionHint create(DirectionHintRequestDTO request) {
        validate(request);

        // Resolve and verify the target edge exists.
        NavigationEdge edge = ServiceUtil.findOrThrow(
                navigationEdgeRepository, request.edgeId(), EDGE_NAME);

        // Enforce one DirectionHint per edge (edge column is unique).
        if (directionHintRepository.existsByEdgeId(request.edgeId())) {
            throw new ConflictException(
                    "A direction hint already exists for edge ID " + request.edgeId());
        }

        DirectionHint hint = toEntity(request, edge);
        return directionHintRepository.save(hint);
    }

    // -------------------------------------------------------------------------
    // BATCH CREATE (all-or-nothing)
    // -------------------------------------------------------------------------

    /**
     * Creates many direction hints in a single transaction.
     * All-or-nothing: if ANY item fails validation, references a missing edge,
     * or violates the one-hint-per-edge rule (against the DB or another item in
     * the same batch), the entire batch is rolled back and nothing is saved.
     */
    @Transactional
    public List<DirectionHint> createBatch(List<DirectionHintRequestDTO> requests) {
        ValidationUtil.requireNotEmpty(requests, "Batch must contain at least one direction hint");

        // Guard against duplicate edge IDs within the batch itself, since each
        // edge may hold at most one hint.
        Set<Long> seenEdgeIds = new HashSet<>();
        List<DirectionHint> toSave = new ArrayList<>(requests.size());

        for (int i = 0; i < requests.size(); i++) {
            DirectionHintRequestDTO request = requests.get(i);
            String position = "Item " + (i + 1) + ": ";

            ValidationUtil.requireNonNull(request, position + "request body is required");
            validate(request);

            if (!seenEdgeIds.add(request.edgeId())) {
                throw new ConflictException(
                        position + "duplicate edge ID " + request.edgeId() + " within the batch");
            }

            NavigationEdge edge = ServiceUtil.findOrThrow(
                    navigationEdgeRepository, request.edgeId(), EDGE_NAME);

            if (directionHintRepository.existsByEdgeId(request.edgeId())) {
                throw new ConflictException(
                        position + "a direction hint already exists for edge ID " + request.edgeId());
            }

            toSave.add(toEntity(request, edge));
        }

        return directionHintRepository.saveAll(toSave);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public DirectionHint getById(Long id) {
        return ServiceUtil.findOrThrow(directionHintRepository, id, ENTITY_NAME);
    }

    @Transactional(readOnly = true)
    public DirectionHint getByEdgeId(Long edgeId) {
        ValidationUtil.requirePositive(edgeId, "Edge ID must be a positive value");
        return directionHintRepository.findByEdgeId(edgeId)
                .orElseThrow(() -> new app.radix.navigationSystem.exceptions.NotFoundException(
                        "No direction hint found for edge ID " + edgeId));
    }

    @Transactional(readOnly = true)
    public Page<DirectionHint> getAll(int page, int size, String sortBy) {
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "priority" : sortBy;
        return ServiceUtil.findAllPaged(directionHintRepository, page, size, safeSortBy);
    }

    // -------------------------------------------------------------------------
    // UPDATE (partial)
    // -------------------------------------------------------------------------

    @Transactional
    public DirectionHint update(Long id, DirectionHintRequestDTO request) {
        ValidationUtil.requireNonNull(request, "Request body is required");

        DirectionHint hint = ServiceUtil.findOrThrow(directionHintRepository, id, ENTITY_NAME);

        // edgeId: re-point the hint to a different edge.
        // Validate it exists and isn't already taken by another hint.
        ServiceUtil.setIfPresent(
                request.edgeId(),
                edgeId -> {
                    NavigationEdge edge = ServiceUtil.findOrThrow(
                            navigationEdgeRepository, edgeId, EDGE_NAME);
                    hint.setEdge(edge);
                },
                edgeId -> {
                    ValidationUtil.requirePositive(edgeId, "Edge ID must be a positive value");
                    directionHintRepository.findByEdgeId(edgeId).ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new ConflictException(
                                    "A direction hint already exists for edge ID " + edgeId);
                        }
                    });
                });

        ServiceUtil.setIfPresent(request.direction(), hint::setDirection);

        ServiceUtil.setIfPresent(
                request.hintText(),
                value -> hint.setHintText(value.trim()),
                value -> {
                    ValidationUtil.requireNonBlank(value, "Hint text cannot be blank");
                    ValidationUtil.requireMaxLength(value, MAX_HINT_LENGTH,
                            "Hint text must be at most " + MAX_HINT_LENGTH + " characters");
                });

        ServiceUtil.setIfPresent(
                request.landmarkReference(),
                hint::setLandmarkReference,
                value -> ValidationUtil.requireMaxLength(value, MAX_LANDMARK_LENGTH,
                        "Landmark reference must be at most " + MAX_LANDMARK_LENGTH + " characters"));

        ServiceUtil.setIfPresent(
                request.priority(),
                hint::setPriority,
                value -> ValidationUtil.requireNonNegative(value, "Priority must be zero or greater"));

        return directionHintRepository.save(hint);
    }

    // -------------------------------------------------------------------------
    // DELETE (hard)
    // -------------------------------------------------------------------------

    @Transactional
    public void delete(Long id) {
        ServiceUtil.assertExists(directionHintRepository, id, ENTITY_NAME);
        directionHintRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    /**
     * Builds a DirectionHint entity from a validated request and resolved edge.
     * Shared by single create and batch create.
     */
    private DirectionHint toEntity(DirectionHintRequestDTO request, NavigationEdge edge) {
        DirectionHint hint = new DirectionHint();

        hint.setEdge(edge);
        hint.setDirection(request.direction());
        hint.setHintText(request.hintText().trim());
        hint.setLandmarkReference(request.landmarkReference());
        hint.setPriority(request.priority() != null ? request.priority() : 0);

        return hint;
    }

    private void validate(DirectionHintRequestDTO request) {
        ValidationUtil.requireNonNull(request, "Request body is required");

        ValidationUtil.requireNonNull(request.edgeId(), "Edge ID is required");
        ValidationUtil.requirePositive(request.edgeId(), "Edge ID must be a positive value");

        ValidationUtil.requireNonNull(request.direction(), "Direction is required");

        ValidationUtil.requireNonBlank(request.hintText(), "Hint text is required");
        ValidationUtil.requireMaxLength(request.hintText(), MAX_HINT_LENGTH,
                "Hint text must be at most " + MAX_HINT_LENGTH + " characters");

        ValidationUtil.requireMaxLength(request.landmarkReference(), MAX_LANDMARK_LENGTH,
                "Landmark reference must be at most " + MAX_LANDMARK_LENGTH + " characters");

        if (request.priority() != null) {
            ValidationUtil.requireNonNegative(request.priority(), "Priority must be zero or greater");
        }
    }
}
