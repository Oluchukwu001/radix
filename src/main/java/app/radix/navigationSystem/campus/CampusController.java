package app.radix.navigationSystem.campus;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/campuses")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;
    private final CampusMapper campusMapper;

    /**
     * Create a new campus. Returns 201 Created.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampusResponseDTO create(@RequestBody CampusRequestDTO request) {
        return campusMapper.toResponse(campusService.create(request));
    }

    /**
     * Fetch a single campus by ID.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CampusResponseDTO getById(@PathVariable Long id) {
        return campusMapper.toResponse(campusService.getById(id));
    }

    /**
     * List campuses with pagination and sorting.
     * Defaults: page=0, size=20 (capped at 100 by the service), sortBy=name.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<CampusResponseDTO> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return campusService
                .getAll(page, size, sortBy, sortDir)
                .map(campusMapper::toResponse);
    }

    /**
     * Partial update — only non-null fields in the request body are applied.
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CampusResponseDTO update(
            @PathVariable Long id,
            @RequestBody CampusRequestDTO request) {

        return campusMapper.toResponse(campusService.update(id, request));
    }

    /**
     * Soft-delete a campus. Returns 204 No Content.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        campusService.delete(id);
    }
}
