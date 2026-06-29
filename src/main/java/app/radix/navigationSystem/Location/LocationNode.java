package app.radix.navigationSystem.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.radix.navigationSystem.campus.Campus;
import app.radix.navigationSystem.utils.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "location_nodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor


public class LocationNode extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String nodeCode;

    @Column(nullable = false)
    private String name;

    private String displayName;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType locationType;

   
    private boolean destinationAllowed = true;

    private boolean scanOriginAllowed = true;

    
    private boolean navigableNode = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private LocationNode parent;

    @OneToMany(mappedBy = "parent")
    
    private List<LocationNode> children = new ArrayList<>();

    private Double latitude;

    private Double longitude;

    private Double altitude;

    private Integer floorLevel;

    @ElementCollection
    @CollectionTable(name = "location_aliases")
    private Set<String> aliases = new HashSet<>();
}