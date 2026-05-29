package casualgames.userservice.repository;

import casualgames.userservice.domain.entity.SubscriptionPlan;
import com.security_starter.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByStatus(Status status);
}
