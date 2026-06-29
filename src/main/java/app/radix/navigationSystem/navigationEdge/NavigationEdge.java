package app.radix.navigationSystem.navigationEdge;

import app.radix.navigationSystem.Location.LocationNode;
import app.radix.navigationSystem.utils.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "navigation_edges", uniqueConstraints = @UniqueConstraint(columnNames = { "from_node_id", "to_node_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class NavigationEdge extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_node_id")
    private LocationNode fromNode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_node_id")
    private LocationNode toNode;

    @Column(nullable = false)
    private Double distanceMeters;

    @Column(nullable = false)
    private Integer estimatedWalkTimeSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PathType pathType;

   
    private boolean bidirectional = true;

   
    private boolean accessible = true;

    
    private boolean active = true;
}