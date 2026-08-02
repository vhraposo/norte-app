package com.norte.controller;

import com.norte.dto.AnswerSubmission;
import com.norte.dto.FeedbackRequest;
import com.norte.dto.RecommendationResponse;
import com.norte.entity.User;
import com.norte.security.CurrentUserHolder;
import com.norte.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public RecommendationResponse create(@RequestBody AnswerSubmission submission) {
        User user = CurrentUserHolder.get();
        return recommendationService.createNew(user, submission.getAnswers());
    }

    @GetMapping
    public List<RecommendationResponse> history() {
        User user = CurrentUserHolder.get();
        return recommendationService.history(user);
    }

    @GetMapping("/{id}")
    public RecommendationResponse getOne(@PathVariable Long id) {
        User user = CurrentUserHolder.get();
        return recommendationService.getOne(user, id);
    }

    @PostMapping("/{id}/feedback")
    public RecommendationResponse feedback(@PathVariable Long id, @RequestBody FeedbackRequest req) {
        User user = CurrentUserHolder.get();
        return recommendationService.submitFeedback(user, id, req.isAgrees(), req.getComment());
    }
}
