package com.security_service.repository;

import com.security_service.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByAttributeAndOperation(String attribute, String operation);

    boolean existsByAttributeAndOperation(String attribute, String operation);
}
