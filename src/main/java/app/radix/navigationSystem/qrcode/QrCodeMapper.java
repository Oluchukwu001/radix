package app.radix.navigationSystem.qrcode;

import java.util.List;

import org.mapstruct.Mapper;

import app.radix.navigationSystem.Location.LocationNodeMapper;

/**
 * Maps QrCode entities to response DTOs.
 * Reuses LocationNodeMapper (via `uses`) to turn the linked node into a
 * LocationNodeSummaryDTO, breaking the entity graph so the response never
 * recurses.
 */
@Mapper(
        componentModel = "spring",
        uses = { LocationNodeMapper.class })
public interface QrCodeMapper {

    QrCodeResponseDTO toResponse(QrCode qrCode);

    List<QrCodeResponseDTO> toResponseList(List<QrCode> qrCodes);
}
