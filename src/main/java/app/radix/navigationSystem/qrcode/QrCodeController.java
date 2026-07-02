package app.radix.navigationSystem.qrcode;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/v1/qr-codes")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final QrCodeMapper qrCodeMapper;

    /**
     * Create a new QR code. Returns 201 Created.
     * qrUuid is optional in the body; the server generates one if omitted.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QrCodeResponseDTO create(@RequestBody QrCodeRequestDTO request) {
        return qrCodeMapper.toResponse(qrCodeService.create(request));
    }

    /**
     * Fetch a QR code by ID.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public QrCodeResponseDTO getById(@PathVariable Long id) {
        return qrCodeMapper.toResponse(qrCodeService.getById(id));
    }

    /**
     * Fetch a QR code by its unique UUID.
     */
    @GetMapping("/by-uuid/{qrUuid}")
    @ResponseStatus(HttpStatus.OK)
    public QrCodeResponseDTO getByUuid(@PathVariable String qrUuid) {
        return qrCodeMapper.toResponse(qrCodeService.getByUuid(qrUuid));
    }

    /**
     * List QR codes with pagination and sorting.
     * Defaults: page=0, size=20 (capped at 100 by the service), sortBy=id.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<QrCodeResponseDTO> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        return qrCodeService.getAll(page, size, sortBy)
                .map(qrCodeMapper::toResponse);
    }

    /**
     * List all QR codes attached to a location node.
     */
    @GetMapping("/by-location/{locationNodeId}")
    @ResponseStatus(HttpStatus.OK)
    public List<QrCodeResponseDTO> getByLocationNode(@PathVariable Long locationNodeId) {
        return qrCodeMapper.toResponseList(qrCodeService.getByLocationNode(locationNodeId));
    }

    /**
     * Register a scan of a QR code by UUID.
     * Increments the scan count and updates lastScannedAt.
     */
    @PostMapping("/{qrUuid}/scan")
    @ResponseStatus(HttpStatus.OK)
    public QrCodeResponseDTO scan(@PathVariable String qrUuid) {
        return qrCodeMapper.toResponse(qrCodeService.scan(qrUuid));
    }

    /**
     * Partial update — only non-null fields in the request body are applied.
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public QrCodeResponseDTO update(
            @PathVariable Long id,
            @RequestBody QrCodeRequestDTO request) {

        return qrCodeMapper.toResponse(qrCodeService.update(id, request));
    }

    /**
     * Activate a QR code.
     */
    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    public QrCodeResponseDTO activate(@PathVariable Long id) {
        return qrCodeMapper.toResponse(qrCodeService.activate(id));
    }

    /**
     * Deactivate a QR code.
     */
    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    public QrCodeResponseDTO deactivate(@PathVariable Long id) {
        return qrCodeMapper.toResponse(qrCodeService.deactivate(id));
    }

    /**
     * Delete a QR code. Returns 204 No Content.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        qrCodeService.delete(id);
    }
}
