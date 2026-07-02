package app.radix.navigationSystem.navigationsession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NavigationSessionRepository extends JpaRepository<NavigationSession, Long> {

    Optional<NavigationSession> findBySessionToken(UUID sessionToken);

    boolean existsBySessionToken(UUID sessionToken);

    List<NavigationSession> findByStatus(NavigationStatus status);
}
