package com.norte.controller;

import com.norte.dto.QuestionDto;
import com.norte.entity.Goal;
import com.norte.entity.User;
import com.norte.security.CurrentUserHolder;
import com.norte.service.GoalService;
import com.norte.service.QuestionnaireService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questionnaire")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;
    private final GoalService goalService;

    public QuestionnaireController(QuestionnaireService questionnaireService, GoalService goalService) {
        this.questionnaireService = questionnaireService;
        this.goalService = goalService;
    }

    // Gera o questionario com base nas metas ATIVAS atuais do usuario.
    // Sempre que o usuario adiciona/remove metas, um novo GET aqui reflete o perfil atualizado.
    @GetMapping
    public List<QuestionDto> get() {
        User user = CurrentUserHolder.get();
        List<String> goals = goalService.listActive(user).stream()
            .map(Goal::getDescription)
            .collect(Collectors.toList());
        return questionnaireService.pickQuestions(goals);
    }
}
