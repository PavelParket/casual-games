package casualgames.userservice.repository;

import casualgames.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGuid(UUID guid);

    boolean existsByGuid(UUID guid);

    void deleteByGuid(UUID guid);

    @Query(value = """
            SELECT *
            FROM users
            WHERE guid IN :guids
            FOR UPDATE
            """, nativeQuery = true)
    List<User> findAllByGuidWithLock(@Param("guids") Iterable<UUID> guids);

    @Query(value = """
            SELECT *
            FROM users
            WHERE (:username IS NULL OR username ILIKE CONCAT('%', :username, '%'))
            AND (:status IS NULL OR status = :status)
            """, nativeQuery = true)
    List<User> search(String username, String status);
}