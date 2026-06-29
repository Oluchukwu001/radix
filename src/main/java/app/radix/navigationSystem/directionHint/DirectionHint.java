package app.radix.navigationSystem.directionHint;

import app.radix.navigationSystem.navigationEdge.NavigationEdge;
import app.radix.navigationSystem.utils.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "direction_hints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class DirectionHint extends BaseEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "edge_id", unique = true)
    private NavigationEdge edge;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DirectionEnum direction;

    @Column(nullable = false, length = 500)
    private String hintText;

    private String landmarkReference;

    
    private Integer priority = 0;
}