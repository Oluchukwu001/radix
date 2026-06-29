package app.radix.navigationSystem.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.radix.navigationSystem.campus.Campus;
import app.radix.navigationSystem.campus.CampusRepository;
import app.radix.navigationSystem.exceptions.ConflictException;
import app.radix.navigationSystem.utils.ServiceUtil;
import app.radix.navigationSystem.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationNodeService {

    private static final String ENTITY_NAME = "LocationNode";
    private static final String CAMPUS_NAME = "Campus";
    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    private final LocationNodeRepository locationNodeRepository;
    private final CampusRepository campusRepository;

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Transactional
    public LocationNode create(LocationNodeRequestDTO request) {
        validate(request);

        // nodeCode must be globally unique.
        if (locationNodeRepository.existsByNodeCode(request.nodeCode())) {
            throw new ConflictException(
                    "Location node code '" + request.nodeCode() + "' already exists");
        }

        Campus campus = ServiceUtil.findOrThrow(
                campusRepository, request.campusId(), CAMPUS_NAME);

        LocationNode parent = resolveParent(request.parentId(), null);

        LocationNode node = toEntity(request, campus, parent);
        return locationNodeRepository.save(node);
    }

    // -------------------------------------------------------------------------
    // BATCH CREATE (all-or-nothing)
    // -------------------------------------------------------------------------

    /**
     * Creates many location nodes in a single transaction.
     * All-or-nothing: if ANY item fails validation, references a missing campus
     * or parent, or duplicates a nodeCode (against the DB or another item in the
     * same batch), the entire batch is rolled back and nothing is saved.
     *
     * Note: a parentId in one item cannot reference another item in the SAME
     * batch (those nodes have no IDs until persisted). Parents must already exist
     * in the database. Create parent levels first, then their children.
     */
    @Transactional
    public List<LocationNode> createBatch(List<LocationNodeRequestDTO> requests) {
        ValidationUtil.requireNotEmpty(requests, "Batch must contain at least one location node");

        Set<String> seenCodes = new HashSet<>();
        List<LocationNode> toSave = new ArrayList<>(requests.size());

        for (int i = 0; i < requests.size(); i++) {
            LocationNodeRequestDTO request = requests.get(i);
            String position = "Item " + (i + 1) + ": ";

            ValidationUtil.requireNonNull(request, position + "request body is required");
            validate(request);

            // Duplicate nodeCode within the batch (case-sensitive, matches column).
            if (!seenCodes.add(request.nodeCode())) {
                throw new ConflictException(
                        position + "duplicate node code '" + request.nodeCode() + "' within the batch");
            }

            if (locationNodeRepository.existsByNodeCode(request.nodeCode())) {
                throw new ConflictException(
                        position + "location node code '" + request.nodeCode() + "' already exists");
            }

            Campus campus = ServiceUtil.findOrThrow(
                    campusRepository, request.campusId(), CAMPUS_NAME);

            LocationNode parent = resolveParent(request.parentId(), null);

            toSave.add(toEntity(request, campus, parent));
        }

        return locationNodeRepository.saveAll(toSave);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public LocationNode getById(Long id) {
        return ServiceUtil.findOrThrow(locationNodeRepository, id, ENTITY_NAME);
    }

    @Transactional(readOnly = true)
    public Page<LocationNode> getAll(int page, int size, String sortBy) {
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "name" : sortBy;
        return ServiceUtil.findAllPaged(locationNodeRepository, page, size, safeSortBy);
    }

    @Transactional(readOnly = true)
    public List<LocationNode> getByCampus(Long campusId) {
        ValidationUtil.requirePositive(campusId, "Campus ID must be a positive value");
        ServiceUtil.assertExists(campusRepository, campusId, CAMPUS_NAME);
        return locationNodeRepository.findByCampusId(campusId);
    }

    @Transactional(readOnly = true)
    public List<LocationNode> getChildren(Long parentId) {
        ValidationUtil.requirePositive(parentId, "Parent ID must be a positive value");
        ServiceUtil.assertExists(locationNodeRepository, parentId, ENTITY_NAME);
        return locationNodeRepository.findByParentId(parentId);
    }

    // -------------------------------------------------------------------------
    // UPDATE (partial)
    // -------------------------------------------------------------------------

    @Transactional
    public LocationNode update(Long id, LocationNodeRequestDTO request) {
        ValidationUtil.requireNonNull(request, "Request body is required");

        LocationNode node = ServiceUtil.findOrThrow(locationNodeRepository, id, ENTITY_NAME);

        ServiceUtil.setIfPresent(
                request.nodeCode(),
                value -> node.setNodeCode(value.trim()),
                value -> {
                    ValidationUtil.requireNonBlank(value, "Node code cannot be blank");
                    if (!value.equals(node.getNodeCode())
                            && locationNodeRepository.existsByNodeCode(value)) {
                        throw new ConflictException(
                                "Location node code '" + value + "' already exists");
                    }
                });

        ServiceUtil.setIfPresent(
                request.name(),
                value -> node.setName(value.trim()),
                value -> {
                    ValidationUtil.requireNonBlank(value, "Name cannot be blank");
                    ValidationUtil.requireMaxLength(value, MAX_NAME_LENGTH,
                            "Name must be at most " + MAX_NAME_LENGTH + " characters");
                });

        ServiceUtil.setIfPresent(request.displayName(), node::setDisplayName);

        ServiceUtil.setIfPresent(
                request.description(),
                node::setDescription,
                value -> ValidationUtil.requireMaxLength(value, MAX_DESCRIPTION_LENGTH,
                        "Description must be at most " + MAX_DESCRIPTION_LENGTH + " characters"));

        ServiceUtil.setIfPresent(request.locationType(), node::setLocationType);
        ServiceUtil.setIfPresent(request.destinationAllowed(), node::setDestinationAllowed);
        ServiceUtil.setIfPresent(request.scanOriginAllowed(), node::setScanOriginAllowed);
        ServiceUtil.setIfPresent(request.navigableNode(), node::setNavigableNode);

        ServiceUtil.setIfPresent(
                request.campusId(),
                campusId -> {
                    Campus campus = ServiceUtil.findOrThrow(campusRepository, campusId, CAMPUS_NAME);
                    node.setCampus(campus);
                });

        // Re-parent. Guard against self-parenting and missing parents.
        ServiceUtil.setIfPresent(
                request.parentId(),
                parentId -> node.setParent(resolveParent(parentId, id)));

        ServiceUtil.setIfPresent(
                request.latitude(),
                node::setLatitude,
                value -> ValidationUtil.requireValidLatitude(value, "Latitude must be between -90 and 90"));

        ServiceUtil.setIfPresent(
                request.longitude(),
                node::setLongitude,
                value -> ValidationUtil.requireValidLongitude(value, "Longitude must be between -180 and 180"));

        ServiceUtil.setIfPresent(request.altitude(), node::setAltitude);
        ServiceUtil.setIfPresent(request.floorLevel(), node::setFloorLevel);
        ServiceUtil.setIfPresent(request.aliases(), node::setAliases);

        return locationNodeRepository.save(node);
    }

    // -------------------------------------------------------------------------
    // DELETE (hard)
    // -------------------------------------------------------------------------

    @Transactional
    public void delete(Long id) {
        LocationNode node = ServiceUtil.findOrThrow(locationNodeRepository, id, ENTITY_NAME);

        // Block deletion if this node still has children to avoid orphans.
        if (!locationNodeRepository.findByParentId(id).isEmpty()) {
            throw new ConflictException(
                    "Cannot delete location node " + id + " because it has child nodes");
        }
        locationNodeRepository.delete(node);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    /**
     * Resolves an optional parent node.
     * Returns null when parentId is null. Otherwise verifies it exists and is
     * not the node being updated (no self-parenting).
     */
    private LocationNode resolveParent(Long parentId, Long selfId) {
        if (parentId == null) {
            return null;
        }
        ValidationUtil.requirePositive(parentId, "Parent ID must be a positive value");
        if (selfId != null && parentId.equals(selfId)) {
            throw new ConflictException("A location node cannot be its own parent");
        }
        return ServiceUtil.findOrThrow(locationNodeRepository, parentId, ENTITY_NAME + " parent");
    }

    private LocationNode toEntity(LocationNodeRequestDTO request, Campus campus, LocationNode parent) {
        LocationNode node = new LocationNode();

        node.setNodeCode(request.nodeCode().trim());
        node.setName(request.name().trim());
        node.setDisplayName(request.displayName());
        node.setDescription(request.description());
        node.setLocationType(request.locationType());

        node.setDestinationAllowed(
                request.destinationAllowed() == null || request.destinationAllowed());

        node.setScanOriginAllowed(
                request.scanOriginAllowed() == null || request.scanOriginAllowed());

        node.setNavigableNode(
                request.navigableNode() == null || request.navigableNode());

        node.setCampus(campus);
        node.setParent(parent);

        node.setLatitude(request.latitude());
        node.setLongitude(request.longitude());
        node.setAltitude(request.altitude());
        node.setFloorLevel(request.floorLevel());

        node.setAliases(
                request.aliases() != null
                        ? new HashSet<>(request.aliases())
                        : new HashSet<>());

        return node;
    }

    private void validate(LocationNodeRequestDTO request) {
        ValidationUtil.requireNonNull(request, "Request body is required");

        ValidationUtil.requireNonBlank(request.nodeCode(), "Node code is required");

        ValidationUtil.requireNonBlank(request.name(), "Name is required");
        ValidationUtil.requireMaxLength(request.name(), MAX_NAME_LENGTH,
                "Name must be at most " + MAX_NAME_LENGTH + " characters");

        ValidationUtil.requireMaxLength(request.description(), MAX_DESCRIPTION_LENGTH,
                "Description must be at most " + MAX_DESCRIPTION_LENGTH + " characters");

        ValidationUtil.requireNonNull(request.locationType(), "Location type is required");

        ValidationUtil.requireNonNull(request.campusId(), "Campus ID is required");
        ValidationUtil.requirePositive(request.campusId(), "Campus ID must be a positive value");

        if (request.latitude() != null) {
            ValidationUtil.requireValidLatitude(request.latitude(), "Latitude must be between -90 and 90");
        }
        if (request.longitude() != null) {
            ValidationUtil.requireValidLongitude(request.longitude(), "Longitude must be between -180 and 180");
        }
    }
}
