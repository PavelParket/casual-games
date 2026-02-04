package casualgames.userservice.repository;

import casualgames.userservice.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByGuid(UUID guid);

    boolean existsByGuid(UUID guid);

    void deleteByGuid(UUID guid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT u
            FROM User u
            WHERE guid IN :guids
            """)
    List<User> findAllByGuidWithLock(@Param("guids") Iterable<UUID> guids);
}