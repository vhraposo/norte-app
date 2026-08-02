package com.norte.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.norte.dto.GeneratedPlan;
import com.norte.dto.PlanResponse;
import com.norte.dto.PlanStepDto;
import com.norte.entity.Goal;
import com.norte.entity.Plan;
import com.norte.entity.Recommendation;
import com.norte.entity.User;
import com.norte.exception.ApiException;
import com.norte.repository.PlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlanService {

    private final PlanRepository planRepository;
    private final AiService aiService;
    private final GoalService goalService;
    private final ObjectMapper mapper = new ObjectMapper();

    public PlanService(PlanRepository planRepository, AiService aiService, GoalService goalService) {
        this.planRepository = planRepository;
        this.aiService = aiService;
        this.goalService = goalService;
    }

    // Cria (ou retorna o ja existente) o Diario de Norte para uma recomendacao aceita.
    public PlanResponse getOrCreateForRecommendation(User user, Recommendation rec) {
        return planRepository.findByRecommendationId(rec.getId())
            .map(this::toResponse)
            .orElseGet(() -> create(user, rec));
    }

    private PlanResponse create(User user, Recommendation rec) {
        List<String> allGoals = goalService.listActive(user).stream()
            .map(Goal::getDescription)
            .collect(Collectors.toList());
        if (allGoals.isEmpty()) {
            allGoals = List.of(rec.getTitulo());
        }

        GeneratedPlan generated = aiService.generatePlan(allGoals, rec.getTitulo(), rec.getPorque(), rec.getPrimeiroPasso());

        try {
            Plan plan = new Plan();
            plan.setUser(user);
            plan.setRecommendation(rec);
            plan.setNumber((int) planRepository.countByUser(user) + 1);
            plan.setTitulo(rec.getTitulo());
            plan.setOrdemSugerida(generated.getOrdemSugerida());

            List<PlanStepDto> passos = generated.getPassosTexto().stream()
                .map(texto -> new PlanStepDto(UUID.randomUUID().toString(), texto, false))
                .collect(Collectors.toList());
            plan.setPassosJson(mapper.writeValueAsString(passos));

            planRepository.save(plan);
            return toResponse(plan);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar o Diario de Norte: " + e.getMessage());
        }
    }

    public Long findPlanIdByRecommendation(Long recommendationId) {
        return planRepository.findByRecommendationId(recommendationId).map(Plan::getId).orElse(null);
    }

    public List<PlanResponse> list(User user) {
        return planRepository.findByUserOrderByNumberDesc(user).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public PlanResponse getOne(User user, Long id) {
        return toResponse(findOwned(user, id));
    }

    public PlanResponse toggleStep(User user, Long planId, String stepId, boolean feito) {
        Plan plan = findOwned(user, planId);
        try {
            List<PlanStepDto> passos = List.of(mapper.readValue(plan.getPassosJson(), PlanStepDto[].class));
            boolean found = false;
            for (PlanStepDto p : passos) {
                if (p.getId().equals(stepId)) {
                    p.setFeito(feito);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Passo nao encontrado nesse Diario de Norte");
            }
            plan.setPassosJson(mapper.writeValueAsString(passos));
            planRepository.save(plan);
            return toResponse(plan);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao atualizar o passo: " + e.getMessage());
        }
    }

    private Plan findOwned(User user, Long id) {
        Plan plan = planRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Diario de Norte nao encontrado"));
        if (!plan.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Esse Diario de Norte nao pertence a esse usuario");
        }
        return plan;
    }

    private PlanResponse toResponse(Plan plan) {
        try {
            PlanResponse r = new PlanResponse();
            r.setId(plan.getId());
            r.setNumber(plan.getNumber());
            r.setTitulo(plan.getTitulo());
            r.setOrdemSugerida(plan.getOrdemSugerida());
            r.setCreatedAt(plan.getCreatedAt());
            r.setPassos(List.of(mapper.readValue(plan.getPassosJson(), PlanStepDto[].class)));
            return r;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao ler o Diario de Norte: " + e.getMessage());
        }
    }
}
