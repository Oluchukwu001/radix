package app.radix.navigationSystem.qrcode;

import java.time.LocalDateTime;

import app.radix.navigationSystem.Location.LocationNode;
import app.radix.navigationSystem.utils.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "qr_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class QrCode extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String qrUuid;

    @ManyToOne(optional = false)
    @JoinColumn(name = "location_node_id")
    private LocationNode locationNode;

    private String physicalPlacement;

   
    private Long scanCount = 0L;

    private LocalDateTime lastScannedAt;

   
    private boolean active = true;
}
