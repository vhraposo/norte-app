package com.norte.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.norte.dto.RecommendationResponse;
import com.norte.entity.Goal;
import com.norte.entity.Recommendation;
import com.norte.entity.User;
import com.norte.exception.ApiException;
import com.norte.repository.RecommendationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final GoalService goalService;
    private final AiService aiService;
    private final PlanService planService;
    private final ObjectMapper mapper = new ObjectMapper();

    public RecommendationService(RecommendationRepository recommendationRepository,
                                  GoalService goalService,
                                  AiService aiService,
                                  PlanService planService) {
        this.recommendationRepository = recommendationRepository;
        this.goalService = goalService;
        this.aiService = aiService;
        this.planService = planService;
    }

    public RecommendationResponse createNew(User user, Map<String, String> answers) {
        List<Goal> activeGoals = goalService.listActive(user);
        if (activeGoals.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Adicione ao menos uma vontade antes de gerar o Norte");
        }
        List<String> goalDescriptions = activeGoals.stream().map(Goal::getDescription).collect(Collectors.toList());

        // A IA gera a recomendacao com base nas metas ativas atuais + respostas do questionario.
        // Sempre que o perfil (metas) muda, uma nova rodada de perguntas + uma nova recomendacao
        // (nova versao) refletem o "Norte" atualizado, mantendo o historico anterior.
        RecommendationResponse aiResult = aiService.generate(goalDescriptions, answers);
        return persist(user, null, goalDescriptions, answers, aiResult);
    }

    // Chamado quando o usuario diz se concorda ou nao com o resultado.
    // Se nao concordar e comentar, geramos uma NOVA versao considerando o comentario,
    // ligada a recomendacao original (parent), sem apagar o historico.
    public RecommendationResponse submitFeedback(User user, Long id, boolean agrees, String comment) {
        Recommendation rec = findOwned(user, id);
        rec.setUserAgrees(agrees);
        rec.setUserComment(comment);
        recommendationRepository.save(rec);

        if (agrees) {
            RecommendationResponse response = toResponse(rec);
            // Ao concordar, gera (ou reaproveita) o Diario de Norte com o plano de acao pra esse objetivo.
            response.setPlanId(planService.getOrCreateForRecommendation(user, rec).getId());
            return response;
        }

        if (comment == null || comment.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Conta pra gente o motivo pra podermos ajustar a recomendacao");
        }

        try {
            List<String> goals = mapper.readValue(rec.getGoalsSnapshotJson(), List.class);
            Map<String, String> answers = mapper.readValue(rec.getAnswersSnapshotJson(), Map.class);
            String previousSummary = buildPreviousSummary(rec);

            RecommendationResponse refined = aiService.refine(goals, answers, previousSummary, comment);
            return persist(user, rec, goals, answers, refined);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao reler recomendacao anterior: " + e.getMessage());
        }
    }

    private String buildPreviousSummary(Recommendation rec) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Titulo: ").append(rec.getTitulo()).append("\n");
            sb.append("Motivo: ").append(rec.getPorque()).append("\n");

            if (rec.getProsJson() != null) {
                List<String> pros = mapper.readValue(rec.getProsJson(), List.class);
                sb.append("Pros: ").append(String.join("; ", pros)).append("\n");
            }
            if (rec.getContrasJson() != null) {
                List<String> contras = mapper.readValue(rec.getContrasJson(), List.class);
                sb.append("Contras: ").append(String.join("; ", contras)).append("\n");
            }
            if (rec.getAlternativasJson() != null) {
                RecommendationResponse.Alternativa[] alts = mapper.readValue(
                    rec.getAlternativasJson(), RecommendationResponse.Alternativa[].class);
                sb.append("Alternativas consideradas e por que nao foram escolhidas:\n");
                for (RecommendationResponse.Alternativa alt : alts) {
                    sb.append("- ").append(alt.getTitulo()).append(": ").append(alt.getPorqueNaoAgora()).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            // fallback simples caso algum campo antigo esteja em formato inesperado
            return "Titulo: " + rec.getTitulo() + "\nMotivo: " + rec.getPorque();
        }
    }

    private RecommendationResponse persist(User user, Recommendation parent, List<String> goalDescriptions,
                                            Map<String, String> answers, RecommendationResponse aiResult) {
        try {
            Recommendation rec = new Recommendation();
            rec.setUser(user);
            rec.setParent(parent);
            rec.setVersion((int) recommendationRepository.countByUser(user) + 1);
            rec.setGoalsSnapshotJson(mapper.writeValueAsString(goalDescriptions));
            rec.setAnswersSnapshotJson(mapper.writeValueAsString(answers));
            rec.setTitulo(aiResult.getTitulo());
            rec.setPorque(aiResult.getPorque());
            rec.setProsJson(mapper.writeValueAsString(aiResult.getPros()));
            rec.setContrasJson(mapper.writeValueAsString(aiResult.getContras()));
            rec.setPrimeiroPasso(aiResult.getPrimeiroPasso());
            rec.setAlternativasJson(mapper.writeValueAsString(aiResult.getAlternativas()));
            rec.setReflexao(aiResult.getReflexao());

            recommendationRepository.save(rec);

            aiResult.setId(rec.getId());
            aiResult.setVersion(rec.getVersion());
            aiResult.setGoalsSnapshot(goalDescriptions);
            aiResult.setCreatedAt(rec.getCreatedAt());
            return aiResult;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar recomendacao: " + e.getMessage());
        }
    }

    public List<RecommendationResponse> history(User user) {
        return recommendationRepository.findByUserOrderByVersionDesc(user).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public RecommendationResponse getOne(User user, Long id) {
        return toResponse(findOwned(user, id));
    }

    private Recommendation findOwned(User user, Long id) {
        Recommendation rec = recommendationRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Recomendacao nao encontrada"));
        if (!rec.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Essa recomendacao nao pertence a esse usuario");
        }
        return rec;
    }

    private RecommendationResponse toResponse(Recommendation rec) {
        try {
            RecommendationResponse r = new RecommendationResponse();
            r.setId(rec.getId());
            r.setVersion(rec.getVersion());
            r.setTitulo(rec.getTitulo());
            r.setPorque(rec.getPorque());
            r.setPrimeiroPasso(rec.getPrimeiroPasso());
            r.setReflexao(rec.getReflexao());
            r.setCreatedAt(rec.getCreatedAt());
            r.setUserAgrees(rec.getUserAgrees());
            r.setUserComment(rec.getUserComment());
            r.setGoalsSnapshot(mapper.readValue(rec.getGoalsSnapshotJson(), List.class));
            r.setAlternativas(List.of(mapper.readValue(rec.getAlternativasJson(), RecommendationResponse.Alternativa[].class)));
            r.setPros(rec.getProsJson() != null ? mapper.readValue(rec.getProsJson(), List.class) : List.of());
            r.setContras(rec.getContrasJson() != null ? mapper.readValue(rec.getContrasJson(), List.class) : List.of());
            r.setPlanId(planService.findPlanIdByRecommendation(rec.getId()));
            return r;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao ler recomendacao salva: " + e.getMessage());
        }
    }
}
