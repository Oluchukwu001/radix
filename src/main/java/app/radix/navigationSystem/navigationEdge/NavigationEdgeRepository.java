package app.radix.navigationSystem.navigationEdge;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NavigationEdgeRepository extends JpaRepository<NavigationEdge, Long> {

    /**
     * The (from_node_id, to_node_id) pair is unique.
     * Used to enforce that constraint with friendly conflict messages.
     */
    boolean existsByFromNodeIdAndToNodeId(Long fromNodeId, Long toNodeId);

    List<NavigationEdge> findByFromNodeId(Long fromNodeId);

    List<NavigationEdge> findByToNodeId(Long toNodeId);
}
