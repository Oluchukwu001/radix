package app.radix.navigationSystem.qrcode;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, Long> {

    Optional<QrCode> findByQrUuid(String qrUuid);

    boolean existsByQrUuid(String qrUuid);

    List<QrCode> findByLocationNodeId(Long locationNodeId);
}
