package app.radix.navigationSystem.navigationEdge;

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
@RequestMapping("/api/v1/navigation-edges")
@RequiredArgsConstructor
public class NavigationEdgeController {

    private final NavigationEdgeService navigationEdgeService;
    private final NavigationEdgeMapper navigationEdgeMapper;

    /**
     * Create a new navigation edge. Returns 201 Created.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NavigationEdgeResponseDTO create(@RequestBody NavigationEdgeRequestDTO request) {
        return navigationEdgeMapper.toResponse(navigationEdgeService.create(request));
    }

    /**
     * Create many navigation edges at once. Returns 201 Created.
     * All-or-nothing: if any item fails, the whole batch is rolled back.
     */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<NavigationEdgeResponseDTO> createBatch(@RequestBody List<NavigationEdgeRequestDTO> requests) {
        return navigationEdgeMapper.toResponseList(navigationEdgeService.createBatch(requests));
    }

    /**
     * Fetch a single navigation edge by ID.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public NavigationEdgeResponseDTO getById(@PathVariable Long id) {
        return navigationEdgeMapper.toResponse(navigationEdgeService.getById(id));
    }

    /**
     * List navigation edges with pagination and sorting.
     * Defaults: page=0, size=20 (capped at 100 by the service), sortBy=id.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<NavigationEdgeResponseDTO> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        return navigationEdgeService.getAll(page, size, sortBy)
                .map(navigationEdgeMapper::toResponse);
    }

    /**
     * List edges leaving a node.
     */
    @GetMapping("/from/{fromNodeId}")
    @ResponseStatus(HttpStatus.OK)
    public List<NavigationEdgeResponseDTO> getOutgoing(@PathVariable Long fromNodeId) {
        return navigationEdgeMapper.toResponseList(navigationEdgeService.getOutgoing(fromNodeId));
    }

    /**
     * List edges entering a node.
     */
    @GetMapping("/to/{toNodeId}")
    @ResponseStatus(HttpStatus.OK)
    public List<NavigationEdgeResponseDTO> getIncoming(@PathVariable Long toNodeId) {
        return navigationEdgeMapper.toResponseList(navigationEdgeService.getIncoming(toNodeId));
    }

    /**
     * Partial update — only non-null fields in the request body are applied.
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public NavigationEdgeResponseDTO update(
            @PathVariable Long id,
            @RequestBody NavigationEdgeRequestDTO request) {

        return navigationEdgeMapper.toResponse(navigationEdgeService.update(id, request));
    }

    /**
     * Delete a navigation edge. Returns 204 No Content.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        navigationEdgeService.delete(id);
    }
}
