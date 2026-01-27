package com.security_service.repository;

import com.security_service.domain.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    /**
     * Get all role-permission mappings for Redis sync
     * Returns: [role_name, attribute, operation]
     */
    @Query("""
            SELECT r.name, p.attribute, p.operation
            FROM RolePermission rp
            JOIN rp.role r
            JOIN rp.permission p
            ORDER BY r.name, p.attribute, p.operation
            """)
    List<Object[]> findAllRolePermissions();

    /**
     * Get permissions for specific role
     * Returns: [attribute, operation]
     */
    @Query("""
            SELECT p.attribute, p.operation
            FROM RolePermission rp
            JOIN rp.role r
            JOIN rp.permission p
            WHERE r.name = :roleName
            """)
    List<Object[]> findPermissionsByRoleName(@Param("roleName") String roleName);

    /**
     * Check if role has specific permission
     */
    @Query("""
            SELECT COUNT(rp) > 0
            FROM RolePermission rp
            JOIN rp.role r
            JOIN rp.permission p
            WHERE r.name = :roleName
            AND p.attribute = :attribute
            AND p.operation = :operation
            """)
    boolean existsByRoleNameAndPermission(
            @Param("roleName") String roleName,
            @Param("attribute") String attribute,
            @Param("operation") String operation
    );
}
