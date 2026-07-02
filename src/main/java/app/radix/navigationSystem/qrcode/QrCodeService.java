package app.radix.navigationSystem.qrcode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.radix.navigationSystem.Location.LocationNode;
import app.radix.navigationSystem.Location.LocationNodeRepository;
import app.radix.navigationSystem.exceptions.ConflictException;
import app.radix.navigationSystem.exceptions.NotFoundException;
import app.radix.navigationSystem.utils.ServiceUtil;
import app.radix.navigationSystem.utils.ValidationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    private static final String ENTITY_NAME = "QrCode";
    private static final String NODE_NAME = "LocationNode";
    private static final int MAX_PLACEMENT_LENGTH = 255;

    private final QrCodeRepository qrCodeRepository;
    private final LocationNodeRepository locationNodeRepository;

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Transactional
    public QrCode create(QrCodeRequestDTO request) {
        validate(request);

        LocationNode locationNode = ServiceUtil.findOrThrow(
                locationNodeRepository, request.locationNodeId(), NODE_NAME);

        // qrUuid is unique. Accept the client value if provided, else generate one.
        String qrUuid = (request.qrUuid() != null && !request.qrUuid().isBlank())
                ? request.qrUuid().trim()
                : generateUniqueUuid();

        if (qrCodeRepository.existsByQrUuid(qrUuid)) {
            throw new ConflictException("QR code UUID '" + qrUuid + "' already exists");
        }

        QrCode qrCode = new QrCode();
        qrCode.setQrUuid(qrUuid);
        qrCode.setLocationNode(locationNode);
        qrCode.setPhysicalPlacement(request.physicalPlacement());
        qrCode.setScanCount(0L);
        qrCode.setActive(request.active() == null || request.active());

        return qrCodeRepository.save(qrCode);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public QrCode getById(Long id) {
        return ServiceUtil.findOrThrow(qrCodeRepository, id, ENTITY_NAME);
    }

    @Transactional(readOnly = true)
    public QrCode getByUuid(String qrUuid) {
        ValidationUtil.requireNonBlank(qrUuid, "QR UUID is required");
        return qrCodeRepository.findByQrUuid(qrUuid.trim())
                .orElseThrow(() -> new NotFoundException("QR code not found for the given UUID"));
    }

    @Transactional(readOnly = true)
    public Page<QrCode> getAll(int page, int size, String sortBy) {
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        return ServiceUtil.findAllPaged(qrCodeRepository, page, size, safeSortBy);
    }

    @Transactional(readOnly = true)
    public List<QrCode> getByLocationNode(Long locationNodeId) {
        ValidationUtil.requirePositive(locationNodeId, "Location node ID must be a positive value");
        ServiceUtil.assertExists(locationNodeRepository, locationNodeId, NODE_NAME);
        return qrCodeRepository.findByLocationNodeId(locationNodeId);
    }

    // -------------------------------------------------------------------------
    // SCAN
    // -------------------------------------------------------------------------

    /**
     * Registers a scan of a QR code by its UUID: increments the scan count and
     * stamps lastScannedAt. Scanning an inactive code is rejected.
     */
    @Transactional
    public QrCode scan(String qrUuid) {
        QrCode qrCode = getByUuid(qrUuid);

        if (!qrCode.isActive()) {
            throw new ConflictException("QR code is inactive and cannot be scanned");
        }

        long current = qrCode.getScanCount() != null ? qrCode.getScanCount() : 0L;
        qrCode.setScanCount(current + 1);
        qrCode.setLastScannedAt(LocalDateTime.now());

        return qrCodeRepository.save(qrCode);
    }

    // -------------------------------------------------------------------------
    // UPDATE (partial)
    // -------------------------------------------------------------------------

    @Transactional
    public QrCode update(Long id, QrCodeRequestDTO request) {
        ValidationUtil.requireNonNull(request, "Request body is required");

        QrCode qrCode = ServiceUtil.findOrThrow(qrCodeRepository, id, ENTITY_NAME);

        ServiceUtil.setIfPresent(
                request.qrUuid(),
                value -> qrCode.setQrUuid(value.trim()),
                value -> {
                    ValidationUtil.requireNonBlank(value, "QR UUID cannot be blank");
                    if (!value.equals(qrCode.getQrUuid())
                            && qrCodeRepository.existsByQrUuid(value)) {
                        throw new ConflictException("QR code UUID '" + value + "' already exists");
                    }
                });

        ServiceUtil.setIfPresent(
                request.locationNodeId(),
                nodeId -> qrCode.setLocationNode(
                        ServiceUtil.findOrThrow(locationNodeRepository, nodeId, NODE_NAME)),
                nodeId -> ValidationUtil.requirePositive(nodeId, "Location node ID must be a positive value"));

        ServiceUtil.setIfPresent(
                request.physicalPlacement(),
                qrCode::setPhysicalPlacement,
                value -> ValidationUtil.requireMaxLength(value, MAX_PLACEMENT_LENGTH,
                        "Physical placement must be at most " + MAX_PLACEMENT_LENGTH + " characters"));

        ServiceUtil.setIfPresent(request.active(), qrCode::setActive);

        return qrCodeRepository.save(qrCode);
    }

    // -------------------------------------------------------------------------
    // ACTIVATE / DEACTIVATE
    // -------------------------------------------------------------------------

    @Transactional
    public QrCode activate(Long id) {
        QrCode qrCode = ServiceUtil.findOrThrow(qrCodeRepository, id, ENTITY_NAME);
        qrCode.setActive(true);
        return qrCodeRepository.save(qrCode);
    }

    @Transactional
    public QrCode deactivate(Long id) {
        QrCode qrCode = ServiceUtil.findOrThrow(qrCodeRepository, id, ENTITY_NAME);
        qrCode.setActive(false);
        return qrCodeRepository.save(qrCode);
    }

    // -------------------------------------------------------------------------
    // DELETE (hard)
    // -------------------------------------------------------------------------

    @Transactional
    public void delete(Long id) {
        ServiceUtil.assertExists(qrCodeRepository, id, ENTITY_NAME);
        qrCodeRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private String generateUniqueUuid() {
        String uuid = UUID.randomUUID().toString();
        while (qrCodeRepository.existsByQrUuid(uuid)) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    private void validate(QrCodeRequestDTO request) {
        ValidationUtil.requireNonNull(request, "Request body is required");

        ValidationUtil.requireNonNull(request.locationNodeId(), "Location node ID is required");
        ValidationUtil.requirePositive(request.locationNodeId(), "Location node ID must be a positive value");

        ValidationUtil.requireMaxLength(request.physicalPlacement(), MAX_PLACEMENT_LENGTH,
                "Physical placement must be at most " + MAX_PLACEMENT_LENGTH + " characters");
    }
}
