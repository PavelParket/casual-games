package com.casualgames.securityservice.repository;

import com.casualgames.securityservice.domain.entity.RolePermission;
import com.casualgames.securityservice.repository.projection.PermissionProjection;
import com.casualgames.securityservice.repository.projection.RolePermissionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    @Query(value = """
            SELECT r.name AS roleName,
                    p.attribute AS attribute,
                    p.operation AS operation,
                    rp.for_me AS forMe,
                    rp.for_all AS forAll
            FROM role_permission rp
            JOIN roles r ON rp.role_id = r.id
            JOIN permissions p ON rp.permission_id = p.id
            ORDER BY r.name, p.attribute, p.operation
            """,
            nativeQuery = true)
    List<RolePermissionProjection> findAllRolePermissions();

    @Query(value = """
            SELECT p.attribute AS attribute,
                    p.operation AS operation,
                    rp.for_me AS forMe,
                    rp.for_all AS forAll
            FROM role_permission rp
            JOIN roles r ON rp.role_id = r.id
            JOIN permissions p ON rp.permission_id = p.id
            WHERE r.name = :roleName
            """,
            nativeQuery = true)
    List<PermissionProjection> findPermissionsByRole(String roleName);

    @Query(value = """
                SELECT COUNT(*) > 0
                FROM role_permission rp
                JOIN roles r ON rp.role_id = r.id
                JOIN permissions p ON rp.permission_id = p.id
                WHERE r.name = :roleName
                AND p.attribute = :attribute
                AND p.operation = :operation
            """, nativeQuery = true)
    boolean existsByRoleAndPermission(String roleName, String attribute, String operation);

    List<RolePermission> findAllByRoleId(Long roleId);

    List<RolePermission> findAllByPermissionId(Long permissionId);
}
