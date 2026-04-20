package com.security_service.repository;

import com.security_service.domain.entity.UserPermission;
import com.security_service.repository.projection.FullUserPermissionProjection;
import com.security_service.repository.projection.UserPermissionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    @Query(value = """
            SELECT p.attribute AS attribute,
                    p.operation AS operation,
                    up.for_me AS forMe,
                    up.for_all AS forAll,
                    up.allowed AS allowed
            FROM user_permission up
            JOIN permissions p ON up.permission_id = p.id
            WHERE up.user_guid = :userGuid
            """,
            nativeQuery = true)
    List<UserPermissionProjection> findAllWithPermissionByUserGuid(UUID userGuid);

    List<UserPermission> findAllByPermissionId(Long permissionId);

    Optional<UserPermission> findByUserGuidAndPermissionId(UUID userGuid, Long permissionId);

    boolean existsByUserGuid(UUID userGuid);

    void deleteAllByUserGuid(UUID userGuid);

    @Query(value = """
            SELECT a.email AS email,
                    p.attribute AS attribute,
                    p.operation AS operation,
                    up.for_me AS forMe,
                    up.for_all AS forAll,
                    up.allowed AS allowed
            FROM user_permission up
            JOIN auths a ON up.user_guid = a.guid
            JOIN permissions p ON up.permission_id = p.id
            ORDER BY a.email
            """,
            nativeQuery = true)
    List<FullUserPermissionProjection> findAllForFullSync();
}
