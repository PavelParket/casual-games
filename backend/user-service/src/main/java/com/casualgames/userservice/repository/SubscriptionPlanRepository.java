package com.casualgames.userservice.repository;

import com.casualgames.securitystarter.enums.Status;
import com.casualgames.userservice.domain.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByStatus(Status status);
}
