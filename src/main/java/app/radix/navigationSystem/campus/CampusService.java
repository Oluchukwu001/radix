package app.radix.navigationSystem.campus;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.radix.navigationSystem.utils.ServiceUtil;
import app.radix.navigationSystem.utils.ValidationUtil;

@Service
public class CampusService {

    private static final String ENTITY_NAME = "Campus";

    private final CampusRepository campusRepository;

    public CampusService(CampusRepository campusRepository) {
        this.campusRepository = campusRepository;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Transactional
    public Campus create(CampusRequestDTO request) {
        validate(request);

        // Ensure campusCode is unique before inserting.
        Campus probe = new Campus();
        probe.setCampusCode(request.campusCode());
        ServiceUtil.assertUnique(
                campusRepository,
                buildCodeExample(probe),
                "Campus code '" + request.campusCode() + "'");

        Campus campus = new Campus();
        campus.setCampusCode(request.campusCode().trim());
        campus.setName(request.name().trim());
        campus.setAddress(request.address());
        campus.setDescription(request.description());
        campus.setLatitude(request.latitude());
        campus.setLongitude(request.longitude());

        return campusRepository.save(campus);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Campus getById(Long id) {
        return ServiceUtil.findOrThrow(campusRepository, id, ENTITY_NAME);
    }

    @Transactional(readOnly = true)
    public Page<Campus> getAll(int page, int size, String sortBy) {
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "name" : sortBy;
        return ServiceUtil.findAllPaged(campusRepository, page, size, safeSortBy);
    }

    @Transactional(readOnly = true)
    public Page<Campus> getAll(int page, int size, String sortBy, String sortDir) {

        String safeSortBy = (sortBy == null || sortBy.isBlank())
                ? "name"
                : sortBy;

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sortByClass = Sort.by(direction, safeSortBy);
        Pageable pageable = PageRequest.of(page, size, sortByClass);

        return campusRepository.findAll(pageable);
    }

    // -------------------------------------------------------------------------
    // UPDATE (partial)
    // -------------------------------------------------------------------------

    @Transactional
    public Campus update(Long id, CampusRequestDTO request) {
        ValidationUtil.requireNonNull(request, "Request body is required");

        Campus campus = ServiceUtil.findOrThrow(campusRepository, id, ENTITY_NAME);

        // campusCode: validate, enforce uniqueness against other records, then set.
        ServiceUtil.setIfPresent(
                request.campusCode(),
                value -> campus.setCampusCode(value.trim()),
                value -> {
                    ValidationUtil.requireNonBlank(value, "Campus code cannot be blank");
                    ValidationUtil.requireMaxLength(value, 100, "Campus code must be at most 100 characters");

                    Campus probe = new Campus();
                    probe.setCampusCode(value);
                    ServiceUtil.assertUniqueOnUpdate(
                            campusRepository,
                            buildCodeExample(probe),
                            id,
                            "Campus code '" + value + "'");
                });

        ServiceUtil.setIfPresent(
                request.name(),
                value -> campus.setName(value.trim()),
                value -> {
                    ValidationUtil.requireNonBlank(value, "Name cannot be blank");
                    ValidationUtil.requireMaxLength(value, 150, "Name must be at most 150 characters");
                });

        ServiceUtil.setIfPresent(
                request.address(),
                campus::setAddress,
                value -> ValidationUtil.requireMaxLength(value, 255, "Address must be at most 255 characters"));

        ServiceUtil.setIfPresent(
                request.description(),
                campus::setDescription,
                value -> ValidationUtil.requireMaxLength(value, 2000, "Description must be at most 2000 characters"));

        ServiceUtil.setIfPresent(
                request.latitude(),
                campus::setLatitude,
                value -> ValidationUtil.requireValidLatitude(value, "Latitude must be between -90 and 90"));

        ServiceUtil.setIfPresent(
                request.longitude(),
                campus::setLongitude,
                value -> ValidationUtil.requireValidLongitude(value, "Longitude must be between -180 and 180"));

        return campusRepository.save(campus);
    }

    // -------------------------------------------------------------------------
    // DELETE (soft)
    // -------------------------------------------------------------------------

    @Transactional
    public void delete(Long id) {
        resolveCampusById(id); 
        campusRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private void validate(CampusRequestDTO request) {
        ValidationUtil.requireNonNull(request, "Request body is required");

        ValidationUtil.requireNonBlank(request.campusCode(), "Campus code is required");
        ValidationUtil.requireMaxLength(request.campusCode(), 100, "Campus code must be at most 100 characters");

        ValidationUtil.requireNonBlank(request.name(), "Name is required");
        ValidationUtil.requireMaxLength(request.name(), 150, "Name must be at most 150 characters");

        ValidationUtil.requireMaxLength(request.address(), 255, "Address must be at most 255 characters");
        ValidationUtil.requireMaxLength(request.description(), 2000, "Description must be at most 2000 characters");

        if (request.latitude() != null) {
            ValidationUtil.requireValidLatitude(request.latitude(), "Latitude must be between -90 and 90");
        }
        if (request.longitude() != null) {
            ValidationUtil.requireValidLongitude(request.longitude(), "Longitude must be between -180 and 180");
        }
    }

    private Campus resolveCampusById(Long id) {
        if(!campusRepository.existsById(id)) {
            throw new IllegalArgumentException("Campus with ID " + id + " does not exist.");
        }
        return campusRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Campus with ID " + id + " does not exist."));
    }

    /**
     * Builds an Example that matches a Campus by campusCode only,
     * ignoring all other (null) fields and matching the code exactly.
     */
    private Example<Campus> buildCodeExample(Campus probe) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withMatcher("campusCode", ExampleMatcher.GenericPropertyMatchers.exact());
        return Example.of(probe, matcher);
    }
}
