package casualgames.userservice.repository;

import casualgames.userservice.domain.entity.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Page<Friendship> findAllByUserGuid(UUID userGuid, Pageable pageable);

    boolean existsByUserGuidAndFriendGuid(UUID userGuid, UUID friendGuid);

    // todo: вместо запроса на мутирование, получение id и удаление по ним
    @Modifying
    @Query(value = """
            DELETE FROM friendship
            WHERE (user_guid, friend_guid) IN ((:userGuid, :friendGuid), (:friendGuid, :userGuid))
            """, nativeQuery = true)
    int deleteMutual(UUID userGuid, UUID friendGuid);

    Optional<Friendship> findByUserGuidAndFriendGuid(UUID userGuid, UUID friendGuid);
}
