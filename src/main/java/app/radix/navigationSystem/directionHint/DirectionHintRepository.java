package app.radix.navigationSystem.directionHint;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectionHintRepository extends JpaRepository<DirectionHint, Long> {

    /**
     * A NavigationEdge can have at most one DirectionHint (edge column is unique).
     * Used to enforce the one-hint-per-edge rule with friendly conflict messages.
     */
    boolean existsByEdgeId(Long edgeId);

    Optional<DirectionHint> findByEdgeId(Long edgeId);
}
