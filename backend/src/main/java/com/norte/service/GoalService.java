package com.norte.service;

import com.norte.entity.Goal;
import com.norte.entity.User;
import com.norte.exception.ApiException;
import com.norte.repository.GoalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public List<Goal> listActive(User user) {
        return goalRepository.findByUserAndActiveTrue(user);
    }

    public Goal add(User user, String description) {
        Goal goal = new Goal();
        goal.setUser(user);
        goal.setDescription(description);
        return goalRepository.save(goal);
    }

    public void deactivate(User user, Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Meta nao encontrada"));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Essa meta nao pertence a esse usuario");
        }
        goal.setActive(false);
        goalRepository.save(goal);
    }
}
