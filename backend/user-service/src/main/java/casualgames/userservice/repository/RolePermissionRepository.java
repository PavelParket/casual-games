package casualgames.userservice.repository;

import casualgames.userservice.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM role_permission rp
                JOIN roles r ON r.id = rp.role_id
                JOIN permissions p ON p.id = rp.permission_id
                WHERE r.name = :role
                  AND p.attribute = :permission
                  AND p.operation = :operation
                  AND (rp.for_all = true OR (rp.for_me = true AND :isOwner = true))
            )
            """, nativeQuery = true)
    boolean can(
            @Param("role") String role,
            @Param("permission") String permission,
            @Param("operation") String operation,
            @Param("isOwner") boolean isOwner
    );
}
