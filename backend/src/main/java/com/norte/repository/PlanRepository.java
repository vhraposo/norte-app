package com.norte.repository;

import com.norte.entity.Plan;
import com.norte.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByUserOrderByNumberDesc(User user);
    long countByUser(User user);
    Optional<Plan> findByRecommendationId(Long recommendationId);
}
