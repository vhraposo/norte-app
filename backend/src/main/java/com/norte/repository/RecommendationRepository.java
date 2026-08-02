package com.norte.repository;

import com.norte.entity.Recommendation;
import com.norte.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByUserOrderByVersionDesc(User user);
    long countByUser(User user);
}
