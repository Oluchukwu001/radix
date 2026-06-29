package app.radix.navigationSystem.Location;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationNodeRepository extends JpaRepository<LocationNode, Long> {

    /**
     * nodeCode is unique across all location nodes.
     * Used to enforce uniqueness with friendly conflict messages.
     */
    boolean existsByNodeCode(String nodeCode);

    List<LocationNode> findByCampusId(Long campusId);

    List<LocationNode> findByParentId(Long parentId);
}
