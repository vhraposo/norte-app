package com.norte.controller;

import com.norte.dto.PlanResponse;
import com.norte.dto.ToggleStepRequest;
import com.norte.entity.User;
import com.norte.security.CurrentUserHolder;
import com.norte.service.PlanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public List<PlanResponse> list() {
        User user = CurrentUserHolder.get();
        return planService.list(user);
    }

    @GetMapping("/{id}")
    public PlanResponse getOne(@PathVariable Long id) {
        User user = CurrentUserHolder.get();
        return planService.getOne(user, id);
    }

    @PatchMapping("/{id}/steps/{stepId}")
    public PlanResponse toggleStep(@PathVariable Long id, @PathVariable String stepId, @RequestBody ToggleStepRequest req) {
        User user = CurrentUserHolder.get();
        return planService.toggleStep(user, id, stepId, req.isFeito());
    }
}
