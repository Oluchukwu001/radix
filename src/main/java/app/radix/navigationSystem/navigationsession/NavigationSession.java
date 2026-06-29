package app.radix.navigationSystem.navigationsession;

import java.time.LocalDateTime;
import java.util.UUID;

import app.radix.navigationSystem.Location.LocationNode;
import app.radix.navigationSystem.utils.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "navigation_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class NavigationSession extends BaseEntity {

    @Column(nullable = false, unique = true)
    private UUID sessionToken;

    @ManyToOne(optional = false)
    @JoinColumn(name = "start_node_id")
    private LocationNode startNode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destination_node_id")
    private LocationNode destinationNode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "current_node_id")
    private LocationNode currentNode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NavigationStatus status;

    private Double totalDistanceMeters;

    private Integer estimatedDurationSeconds;

    private String deviceFingerprint;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}