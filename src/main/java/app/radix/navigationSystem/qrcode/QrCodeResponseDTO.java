package app.radix.navigationSystem.qrcode;

import java.time.LocalDateTime;

import app.radix.navigationSystem.Location.LocationNodeSummaryDTO;

/**
 * Full QrCode response. The linked location is exposed as a lightweight
 * LocationNode summary to keep the payload flat and avoid serializing the
 * full node graph (parent/children/campus).
 */
public record QrCodeResponseDTO(
        Long id,
        String qrUuid,
        LocationNodeSummaryDTO locationNode,
        String physicalPlacement,
        Long scanCount,
        LocalDateTime lastScannedAt,
        boolean active
) {
}
