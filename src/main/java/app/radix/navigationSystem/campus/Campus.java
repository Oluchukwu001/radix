package  app.radix.navigationSystem.campus;

// Examples - change the part after 'navigationSystem.' to match your actual folder names!

import app.radix.navigationSystem.Location.LocationNode;
import app.radix.navigationSystem.utils.BaseEntity;

import java.util.ArrayList;
// Java Standard Library Utilities
import java.util.List;

// Jakarta Persistence (JPA) for database annotations
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;

// Project Lombok for boilerplate reduction
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;




@Entity
@Table(name = "campuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Campus extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String campusCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(length = 2000)
    private String description;

    private Double latitude;

    private Double longitude;

    @OneToMany(mappedBy = "campus")

    private List<LocationNode> locationNodes = new ArrayList<>();
}
