package app.radix.navigationSystem.directionHint;

import java.util.List;
import java.util.stream.Collectors;

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
@RequestMapping("/api/v1/direction-hints")
@RequiredArgsConstructor
public class DirectionHintController {

    private final DirectionHintService directionHintService;
    private final DirectionHintMapper directionHintMapper;

   
    /**
     * Create a new direction hint. Returns 201 Created.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectionHintResponseDTO create(@RequestBody DirectionHintRequestDTO request) {
        return directionHintMapper.toResponse(directionHintService.create(request));
    }

    /**
     * Create many direction hints at once. Returns 201 Created.
     * All-or-nothing: if any item fails, the whole batch is rolled back.
     */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<DirectionHintResponseDTO> createBatch(@RequestBody List<DirectionHintRequestDTO> requests) {
        return directionHintService.createBatch(requests).stream()
                .map(directionHintMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Fetch a single direction hint by ID.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DirectionHintResponseDTO getById(@PathVariable Long id) {
        return directionHintMapper.toResponse(directionHintService.getById(id));
    }

    /**
     * Fetch the direction hint attached to a specific edge.
     */
    @GetMapping("/by-edge/{edgeId}")
    @ResponseStatus(HttpStatus.OK)
    public DirectionHintResponseDTO getByEdgeId(@PathVariable Long edgeId) {
        return directionHintMapper.toResponse(directionHintService.getByEdgeId(edgeId));
    }

    /**
     * List direction hints with pagination and sorting.
     * Defaults: page=0, size=20 (capped at 100 by the service), sortBy=priority.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<DirectionHintResponseDTO> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "priority") String sortBy) {

        return directionHintService.getAll(page, size, sortBy).map(directionHintMapper::toResponse);
    }

    /**
     * Partial update — only non-null fields in the request body are applied.
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DirectionHintResponseDTO update(
            @PathVariable Long id,
            @RequestBody DirectionHintRequestDTO request) {

        return directionHintMapper.toResponse(directionHintService.update(id, request));
    }

    /**
     * Delete a direction hint. Returns 204 No Content.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        directionHintService.delete(id);
    }
}
