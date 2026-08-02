package com.norte.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.norte.dto.GeneratedPlan;
import com.norte.dto.QuestionDto;
import com.norte.dto.RecommendationResponse;
import com.norte.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chama a API da Groq (gratuita, https://console.groq.com) para gerar a recomendacao.
 * Groq expoe uma API compativel com o formato OpenAI de chat completions.
 * Modelo padrao: openai/gpt-oss-120b, um modelo de raciocinio de verdade (nao so um chat
 * model rapido) com esforco de raciocinio configuravel via groq.api.reasoning-effort.
 * Para trocar de provedor de IA no futuro, basta reescrever este service.
 */
@Service
public class AiService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.model}")
    private String model;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.reasoning-effort:medium}")
    private String reasoningEffort;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();

    private static final String GRAMMAR_RULE = """

        REGRA DE IDIOMA: escreva em portugues do Brasil gramaticalmente correto. Preste atencao
        especial a concordancia verbal e nominal (genero e numero corretos entre artigos, substantivos,
        adjetivos e verbos). Antes de finalizar cada frase, revise mentalmente se a concordancia esta
        correta. Erros de concordancia (ex: "a pessoa pode ser mais importante" quando deveria ser
        "as pessoas podem ser mais importantes") sao inaceitaveis.
        """;

    private static final String FRAMEWORK_PROMPT = """
        Voce e um assistente que ajuda pessoas a escolher, entre varias vontades/objetivos de vida, \
        qual caminho priorizar agora.
        Use como base estruturas comuns em orientacao psicologica e de carreira, adaptadas de forma leiga e pratica:
        - Clarificacao de valores (o que a pessoa realmente valoriza, nao o que "deveria" valorizar)
        - Motivacao intrinseca vs extrinseca (Teoria da Autodeterminacao)
        - Matriz urgencia x importancia
        - Energia e sustentabilidade: escolher algo que a pessoa consiga manter, nao so comecar
        - Situacao financeira e capacidade real de organizacao/planejamento da pessoa, quando informadas

        Para a recomendacao principal, liste pros e contras honestos e especificos (nao genericos) de seguir
        esse caminho agora, baseados nas respostas da pessoa. Nao esconda contras reais so para parecer
        mais convincente. Os campos "pros" e "contras" sao OBRIGATORIOS e NUNCA podem vir vazios ou ausentes -
        toda recomendacao tem pelo menos 2 pros e 2 contras reais, mesmo que a recomendacao pareca obviamente boa.

        Para cada alternativa que NAO for a recomendacao principal, inclua uma critica honesta e especifica \
        de por que nao e o melhor momento para focar nela agora (nao generica, baseada nas respostas da pessoa).
        O "titulo" de cada alternativa deve ser CURTO (2 a 5 palavras, o nome da vontade, ex: "Fazer intercambio")
        e "porque_nao_agora" deve ser objetivo: 1 frase direta, sem repetir o raciocinio inteiro nem misturar
        varios argumentos numa frase so.

        Responda SOMENTE com um JSON valido, sem markdown, sem texto fora do JSON, no formato exato:
        {
          "titulo": "string curta com o caminho recomendado",
          "porque": "2-3 frases explicando o motivo com base nas respostas",
          "pros": ["prós especificos, 2 a 4 itens curtos"],
          "contras": ["contras especificos e honestos, 2 a 4 itens curtos"],
          "primeiro_passo": "uma acao concreta e pequena para essa semana",
          "alternativas": [{"titulo": "string", "porque_nao_agora": "critica especifica de 1-2 frases"}],
          "reflexao": "uma pergunta reflexiva para a pessoa se conhecer melhor"
        }
        """ + GRAMMAR_RULE;

    private static final String REFINE_SUFFIX = """

        IMPORTANTE: essa pessoa ja recebeu a recomendacao anterior (com pros, contras e criticas as alternativas,
        tudo enviado acima) e discordou dela ou fez uma pergunta sobre ela. Leve o comentario dela muito a serio:

        - Se o comentario for uma PERGUNTA sobre por que uma alternativa especifica nao seria boa (ex: "por que
          X nao seria uma boa opcao?"), isso NAO significa que a pessoa quer que X vire a nova recomendacao
          principal. Responda a pergunta dela dentro do campo "porque" da nova recomendacao, de forma direta,
          e so troque a recomendacao principal para essa alternativa se, apos considerar o comentario, isso
          for genuinamente a decisao mais coerente - explicando explicitamente por que a resposta a pergunta
          dela levou a essa mudanca.
        - Se o comentario for uma DISCORDANCIA de fato (ex: "acho que voces erraram porque..."), ajuste a
          recomendacao de forma coerente com o argumento dela.
        - Nunca troque a recomendacao principal de forma arbitraria ou contraditoria com o proprio comentario
          da pessoa. Se ela questionou uma opcao, sua resposta precisa deixar claro POR QUE aquela opcao
          continua nao sendo a melhor (ou, se for o caso, por que ela passou a ser).
        - Ajuste pros, contras e criticas das alternativas para refletir esse novo contexto.
        """;

    private static final String QUESTIONS_PROMPT = """
        Voce cria perguntas de multipla escolha para ajudar uma pessoa a decidir qual, entre varias vontades
        de vida especificas, priorizar agora.

        Gere perguntas ESPECIFICAS para a combinacao exata de vontades que a pessoa listou - nao perguntas
        genericas de personalidade. Cada pergunta deve ajudar a comparar essas vontades especificas entre si:
        custo, tempo necessario, urgencia real, dependencia de terceiros, reversibilidade, o que a pessoa
        perderia ao escolher uma em vez de outra, etc. Pense nos trade-offs reais ENTRE essas vontades especificas.

        NAO FACA perguntas genericas como "voce prefere agir rapido ou com calma?" ou "voce gosta de
        planejar ou improvisar?" - isso nao usa as vontades especificas da pessoa. Cada pergunta PRECISA
        citar ou se referir diretamente a pelo menos uma das vontades listadas. Exemplo de pergunta BOA
        (supondo vontades "fazer intercambio" e "comprar um carro"): "Se voce escolhesse fazer intercambio
        agora, o carro teria que esperar quanto tempo?" Exemplo de pergunta RUIM (generica demais): "O que e
        mais importante pra voce: o presente ou o futuro?"

        Regras:
        - Gere entre 6 e 9 perguntas.
        - Cada pergunta tem exatamente 4 opcoes de resposta, curtas (max ~8 palavras cada).
        - As perguntas devem ser em portugues do Brasil, tom direto e informal.

        Responda SOMENTE com um JSON valido, sem markdown, sem texto fora do JSON, no formato exato:
        [
          {"id": "ai1", "text": "string da pergunta", "options": ["opcao 1", "opcao 2", "opcao 3", "opcao 4"]}
        ]
        """ + GRAMMAR_RULE;

    private static final String PLAN_PROMPT = """
        Voce cria planos de acao praticos para uma pessoa comecar a executar um objetivo de vida especifico
        que ela decidiu priorizar.

        Gere um checklist de 5 a 9 passos concretos, especificos e em ordem logica (do mais imediato ao mais
        avancado) para essa pessoa comecar a executar o objetivo. Cada passo deve ser uma acao clara e
        pequena o suficiente para caber numa lista de tarefas (ex: "Tirar o passaporte", "Pesquisar 3 escolas
        de intercambio na cidade X e comparar precos", nao algo vago como "se organizar").

        Alem disso, considerando TODOS os objetivos que essa pessoa listou (nao so o priorizado), sugira a
        melhor ordem para ela perseguir esses objetivos ao longo do tempo, explicando o raciocinio (o que
        depende do que, o que e mais urgente, o que pode ser feito em paralelo).

        Responda SOMENTE com um JSON valido, sem markdown, sem texto fora do JSON, no formato exato:
        {
          "passos": ["passo 1 especifico e acionavel", "passo 2", "..."],
          "ordem_sugerida": "2-4 frases explicando a melhor ordem para perseguir todos os objetivos listados e por que"
        }
        """ + GRAMMAR_RULE;

    public List<QuestionDto> generateQuestions(List<String> goals) {
        String userMessage = "Vontades da pessoa: " + String.join(", ", goals) +
            "\n\nGere as perguntas especificas seguindo exatamente o formato JSON pedido.";

        for (int attempt = 1; attempt <= 2; attempt++) {
            String clean = callGroqRaw(QUESTIONS_PROMPT, userMessage, 0.5);
            try {
                JsonNode parsed = mapper.readTree(clean);
                List<QuestionDto> questions = new ArrayList<>();
                int i = 1;
                for (JsonNode q : parsed) {
                    List<String> options = new ArrayList<>();
                    for (JsonNode opt : q.path("options")) options.add(opt.asText());
                    String text = q.path("text").asText();
                    if (options.size() < 2 || text.isBlank()) continue;
                    questions.add(new QuestionDto("ai" + (i++), text, options));
                }
                // Resposta cortada pelo modelo (sem token suficiente) costuma virar uma lista vazia
                // ou muito curta - tenta de novo uma vez antes de desistir e cair no banco fixo.
                if (questions.size() >= 4 || attempt == 2) {
                    return questions;
                }
            } catch (Exception e) {
                if (attempt == 2) return List.of();
            }
        }
        return List.of();
    }

    public RecommendationResponse generate(List<String> goals, Map<String, String> answers) {
        String answersText = formatAnswers(answers);
        String userMessage = "Coisas que a pessoa quer fazer: " + String.join(", ", goals) +
            "\n\nRespostas do questionario:\n" + answersText +
            "\nGere a recomendacao seguindo exatamente o formato JSON pedido.";

        return generateWithRetry(FRAMEWORK_PROMPT, userMessage);
    }

    public RecommendationResponse refine(List<String> goals, Map<String, String> answers,
                                          String previousSummary, String userComment) {
        String answersText = formatAnswers(answers);
        String userMessage = "Coisas que a pessoa quer fazer: " + String.join(", ", goals) +
            "\n\nRespostas do questionario:\n" + answersText +
            "\n\nRecomendacao anterior completa que foi dada:\n" + previousSummary +
            "\n\nA pessoa NAO concordou com essa recomendacao e comentou o seguinte:\n\"" + userComment + "\"" +
            "\n\nGere uma nova recomendacao, seguindo exatamente o formato JSON pedido, respondendo com coerencia ao comentario.";

        return generateWithRetry(FRAMEWORK_PROMPT + REFINE_SUFFIX, userMessage);
    }

    // A IA de raciocinio, de vez em quando, corta a resposta antes de terminar o JSON
    // (gasta o orcamento de tokens "pensando"). Detecta isso e tenta de novo uma vez antes
    // de mostrar erro pro usuario.
    private RecommendationResponse generateWithRetry(String systemPrompt, String userMessage) {
        ApiException lastError = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                RecommendationResponse rec = parseRecommendation(callGroqRaw(systemPrompt, userMessage, 0.6));
                if (rec.getTitulo() != null && !rec.getTitulo().isBlank()
                    && rec.getPorque() != null && !rec.getPorque().isBlank()) {
                    return rec;
                }
                lastError = new ApiException(HttpStatus.BAD_GATEWAY, "A IA retornou uma resposta incompleta");
            } catch (ApiException e) {
                lastError = e;
            }
        }
        throw lastError;
    }

    public GeneratedPlan generatePlan(List<String> allGoals, String titulo, String porque, String primeiroPasso) {
        String userMessage = "Objetivo priorizado: " + titulo + "\nMotivo: " + porque +
            "\nPrimeiro passo ja sugerido anteriormente: " + primeiroPasso +
            "\n\nTodos os objetivos que essa pessoa listou: " + String.join(", ", allGoals) +
            "\n\nGere o plano seguindo exatamente o formato JSON pedido.";

        String clean = callGroqRaw(PLAN_PROMPT, userMessage, 0.5);

        try {
            JsonNode parsed = mapper.readTree(clean);
            GeneratedPlan plan = new GeneratedPlan();
            List<String> passos = new ArrayList<>();
            for (JsonNode p : parsed.path("passos")) passos.add(p.asText());
            plan.setPassosTexto(passos);
            plan.setOrdemSugerida(parsed.path("ordem_sugerida").asText());
            return plan;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Nao foi possivel interpretar o plano gerado: " + e.getMessage());
        }
    }

    private String formatAnswers(Map<String, String> answers) {
        StringBuilder sb = new StringBuilder();
        answers.forEach((q, a) -> sb.append("- ").append(q).append(" -> ").append(a).append("\n"));
        return sb.toString();
    }

    private RecommendationResponse parseRecommendation(String clean) {
        try {
            JsonNode parsed = mapper.readTree(clean);

            RecommendationResponse rec = new RecommendationResponse();
            rec.setTitulo(parsed.path("titulo").asText());
            rec.setPorque(parsed.path("porque").asText());
            rec.setPrimeiroPasso(parsed.path("primeiro_passo").asText());
            rec.setReflexao(parsed.path("reflexao").asText());

            List<String> pros = new ArrayList<>();
            for (JsonNode p : parsed.path("pros")) pros.add(p.asText());
            rec.setPros(pros);

            List<String> contras = new ArrayList<>();
            for (JsonNode c : parsed.path("contras")) contras.add(c.asText());
            rec.setContras(contras);

            List<RecommendationResponse.Alternativa> alternativas = new ArrayList<>();
            for (JsonNode alt : parsed.path("alternativas")) {
                alternativas.add(new RecommendationResponse.Alternativa(
                    alt.path("titulo").asText(), alt.path("porque_nao_agora").asText()));
            }
            rec.setAlternativas(alternativas);

            return rec;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Nao foi possivel interpretar a recomendacao gerada: " + e.getMessage());
        }
    }

    /** Chama a Groq e retorna o conteudo textual (JSON limpo, sem markdown) da resposta. */
    private String callGroqRaw(String systemPrompt, String userMessage, double temperature) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                "GROQ_API_KEY nao configurada. Crie uma chave gratuita em https://console.groq.com/keys");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userMessage)
        ));
        body.put("temperature", temperature);
        // Modelos de raciocinio gastam parte do orcamento de tokens "pensando" antes de responder.
        // Sem um limite generoso, a resposta final as vezes fica cortada no meio do JSON.
        body.put("max_completion_tokens", 4000);
        // reasoning_effort so e reconhecido pelos modelos openai/gpt-oss-* no Groq
        if (model != null && model.contains("gpt-oss")) {
            body.put("reasoning_effort", reasoningEffort);
        }

        try {
            String jsonBody = mapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Erro ao chamar a IA (Groq): " + response.body());
            }

            JsonNode root = mapper.readTree(response.body());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            return content.replaceAll("```json|```", "").trim();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Nao foi possivel falar com a IA: " + e.getMessage());
        }
    }
}
