package com.norte.controller;

import com.norte.dto.GoalRequest;
import com.norte.entity.Goal;
import com.norte.entity.User;
import com.norte.security.CurrentUserHolder;
import com.norte.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public List<Goal> list() {
        User user = CurrentUserHolder.get();
        return goalService.listActive(user);
    }

    @PostMapping
    public Goal add(@Valid @RequestBody GoalRequest req) {
        User user = CurrentUserHolder.get();
        return goalService.add(user, req.getDescription());
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        User user = CurrentUserHolder.get();
        goalService.deactivate(user, id);
    }
}
