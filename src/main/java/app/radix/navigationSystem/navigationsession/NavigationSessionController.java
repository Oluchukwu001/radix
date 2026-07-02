package app.radix.navigationSystem.navigationsession;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/navigation-sessions")
@RequiredArgsConstructor
public class NavigationSessionController {

    private final NavigationSessionService sessionService;
    private final NavigationSessionMapper sessionMapper;

    /**
     * Start a new navigation session. Returns 201 Created.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NavigationSessionResponseDTO start(@RequestBody NavigationSessionRequestDTO request) {
        return sessionMapper.toResponse(sessionService.start(request));
    }

    /**
     * Fetch a session by ID.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public NavigationSessionResponseDTO getById(@PathVariable Long id) {
        return sessionMapper.toResponse(sessionService.getById(id));
    }

    /**
     * Fetch a session by its unique token.
     */
    @GetMapping("/by-token/{token}")
    @ResponseStatus(HttpStatus.OK)
    public NavigationSessionResponseDTO getByToken(@PathVariable UUID token) {
        return sessionMapper.toResponse(sessionService.getByToken(token));
    }

    /**
     * List sessions with pagination and sorting.
     * Defaults: page=0, size=20 (capped at 100 by the service), sortBy=startedAt.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<NavigationSessionResponseDTO> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startedAt") String sortBy) {

        return sessionService.getAll(page, size, sortBy)
                .map(sessionMapper::toResponse);
    }

    /**
     * List sessions filtered by status.
     */
    @GetMapping("/by-status/{status}")
    @ResponseStatus(HttpStatus.OK)
    public List<NavigationSessionResponseDTO> getByStatus(@PathVariable NavigationStatus status) {
        return sessionMapper.toResponseList(sessionService.getByStatus(status));
    }

    /**
     * Advance the session to a new current node.
     */
    @PatchMapping("/{id}/advance")
    @ResponseStatus(HttpStatus.OK)
    public NavigationSessionResponseDTO advance(
            @PathVariable Long id,
            @RequestBody AdvanceRequestDTO request) {

        return sessionMapper.toResponse(sessionService.advance(id, request));
    }

    /**
     * Pause an active session.
     */
    @PatchMapping("/{id}/pause")
    @ResponseStatus(HttpStatus.OK)
    public NavigationSessionResponseDTO pause(@PathVariable Long id) {
        return sessionMapper.toResponse(sessionService.pause(id));
    }

    /**
     * Resume a paused session.
     */
    @PatchMapping("/{id}/resume")
    @ResponseStatus(HttpStatus.OK)
    public NavigationSessionResponseDTO resume(@PathVariable Long id) {
        return sessionMapper.toResponse(sessionService.resume(id));
    }

    /**
     * Reroute the session (new current node and/or destination).
     */
    @PatchMapping("/{id}/reroute")
    @ResponseStatus(HttpStatus.OK)
    public NavigationSessionResponseDTO reroute(
            @PathVariable Long id,
            @RequestBody RerouteRequestDTO request) {

        return sessionMapper.toResponse(sessionService.reroute(id, request));
    }

    /**
     * Complete the session.
     */
    @PatchMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.OK)
    public NavigationSessionResponseDTO complete(@PathVariable Long id) {
        return sessionMapper.toResponse(sessionService.complete(id));
    }

    /**
     * Cancel the session.
     */
    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public NavigationSessionResponseDTO cancel(@PathVariable Long id) {
        return sessionMapper.toResponse(sessionService.cancel(id));
    }

    /**
     * Delete a session. Returns 204 No Content.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        sessionService.delete(id);
    }
}
