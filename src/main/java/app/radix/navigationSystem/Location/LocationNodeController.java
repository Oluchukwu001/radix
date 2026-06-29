package app.radix.navigationSystem.Location;

import java.util.List;

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
@RequestMapping("/api/v1/location-nodes")
@RequiredArgsConstructor
public class LocationNodeController {

    private final LocationNodeService locationNodeService;
    private final LocationNodeMapper locationNodeMapper;

    /**
     * Create a new location node. Returns 201 Created.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationNodeResponseDTO create(@RequestBody LocationNodeRequestDTO request) {
        return locationNodeMapper.toResponse(locationNodeService.create(request));
    }

    /**
     * Create many location nodes at once. Returns 201 Created.
     * All-or-nothing: if any item fails, the whole batch is rolled back.
     */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<LocationNodeResponseDTO> createBatch(@RequestBody List<LocationNodeRequestDTO> requests) {
        return locationNodeMapper.toResponseList(locationNodeService.createBatch(requests));
    }

    /**
     * Fetch a single location node by ID.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LocationNodeResponseDTO getById(@PathVariable Long id) {
        return locationNodeMapper.toResponse(locationNodeService.getById(id));
    }

    /**
     * List location nodes with pagination and sorting.
     * Defaults: page=0, size=20 (capped at 100 by the service), sortBy=name.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<LocationNodeResponseDTO> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy) {

        return locationNodeService.getAll(page, size, sortBy)
                .map(locationNodeMapper::toResponse);
    }

    /**
     * List all nodes belonging to a campus.
     */
    @GetMapping("/by-campus/{campusId}")
    @ResponseStatus(HttpStatus.OK)
    public List<LocationNodeResponseDTO> getByCampus(@PathVariable Long campusId) {
        return locationNodeMapper.toResponseList(locationNodeService.getByCampus(campusId));
    }

    /**
     * List the direct children of a node.
     */
    @GetMapping("/{id}/children")
    @ResponseStatus(HttpStatus.OK)
    public List<LocationNodeResponseDTO> getChildren(@PathVariable Long id) {
        return locationNodeMapper.toResponseList(locationNodeService.getChildren(id));
    }

    /**
     * Partial update — only non-null fields in the request body are applied.
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LocationNodeResponseDTO update(
            @PathVariable Long id,
            @RequestBody LocationNodeRequestDTO request) {

        return locationNodeMapper.toResponse(locationNodeService.update(id, request));
    }

    /**
     * Delete a location node. Returns 204 No Content.
     * Fails with a conflict if the node still has children.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        locationNodeService.delete(id);
    }
}
