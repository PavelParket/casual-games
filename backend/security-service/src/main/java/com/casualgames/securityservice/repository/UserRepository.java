package com.casualgames.securityservice.repository;

import com.casualgames.securityservice.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGuid(UUID guid);

    boolean existsByEmail(String email);

    boolean existsByGuid(UUID guid);

    void deleteByGuid(UUID guid);
}
