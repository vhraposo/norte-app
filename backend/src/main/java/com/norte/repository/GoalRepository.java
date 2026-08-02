package com.norte.repository;

import com.norte.entity.Goal;
import com.norte.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserAndActiveTrue(User user);
    List<Goal> findByUser(User user);
}
