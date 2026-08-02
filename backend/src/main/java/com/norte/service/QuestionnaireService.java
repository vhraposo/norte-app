package com.norte.service;

import com.norte.dto.QuestionDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuestionnaireService {

    // Nucleo fixo: perguntas que medem algo consistente entre qualquer combinacao de vontades
    // (valores, tolerancia a risco, tempo disponivel, tipo de motivacao). Sempre feitas, para
    // manter uma base comparavel. As perguntas especificas de cada vontade vem da IA (AiService).
    private final List<QuestionDto> coreGeneral = List.of(
        new QuestionDto("g1", "Daqui a um ano, o que te deixaria mais orgulhoso(a)?",
            List.of("Ter aprendido algo novo e dificil", "Ter vivido uma experiencia marcante",
                    "Ter avancado na carreira ou nos estudos", "Ter fortalecido relacoes importantes")),
        new QuestionDto("g4", "Como voce lida com incerteza e risco?",
            List.of("Gosto do imprevisivel", "Prefiro ter um plano B solido", "Tolero risco calculado", "Prefiro o mais seguro possivel")),
        new QuestionDto("g7", "Quanto tempo por semana voce realisticamente consegue dedicar a algo novo agora?",
            List.of("Menos de 2 horas", "De 2 a 5 horas", "De 5 a 10 horas", "Mais de 10 horas")),
        new QuestionDto("g8", "O que te motiva mais a seguir em frente com algo dificil?",
            List.of("Ver progresso mensuravel", "Ter apoio de outras pessoas", "Ter um prazo ou compromisso externo", "A satisfacao pessoal de fazer")),
        new QuestionDto("g9", "Como esta sua situacao financeira agora?",
            List.of("Bem estruturada, consigo investir", "Equilibrada, mas sem sobra", "Apertada, no limite", "Instavel, varia muito de mes a mes")),
        new QuestionDto("g10", "Como voce costuma se organizar pra colocar planos em pratica?",
            List.of("Sou organizado(a), sigo listas e prazos", "Meio-termo, depende do que for", "Costumo procrastinar bastante", "Prefiro ir na intuicao, sem planejamento"))
    );

    // Banco de perguntas por categoria - usado como FALLBACK caso a chamada a IA falhe
    // (indisponibilidade da Groq, chave nao configurada etc), para o questionario nunca quebrar.

    private final Map<String, List<QuestionDto>> byCategory = Map.ofEntries(
        Map.entry("viagem", List.of(
            new QuestionDto("viagem1", "Sobre viajar, voce prefere...",
                List.of("Roteiro planejado", "Aventura sem plano fixo", "Um pouco dos dois", "Depende do destino")),
            new QuestionDto("viagem2", "Viajar sozinho(a) ou com companhia?",
                List.of("Sozinho(a), sem duvida", "Com companhia de confianca", "Tanto faz", "Prefiro grupos/novas pessoas")),
            new QuestionDto("viagem3", "Voce ja tem o dinheiro guardado ou precisaria juntar antes?",
                List.of("Ja tenho guardado", "Preciso juntar por alguns meses", "Preciso juntar por mais de um ano", "Depende do destino escolhido"))
        )),
        Map.entry("intercambio", List.of(
            new QuestionDto("interc1", "No intercambio, o foco seria mais...",
                List.of("Aprender um idioma a fundo", "Ganhar experiencia internacional pro curriculo",
                        "Viver uma experiencia de vida", "Fazer conexoes e network")),
            new QuestionDto("interc2", "Voce topa ficar fora do pais por quanto tempo?",
                List.of("Algumas semanas", "Alguns meses", "Um ano ou mais", "Ainda nao sei"))
        )),
        Map.entry("show", List.of(
            new QuestionDto("show1", "Voce busca mais experiencias pontuais ou algo recorrente?",
                List.of("Pontuais e intensas", "Recorrentes e constantes", "As duas coisas", "Ainda nao sei")),
            new QuestionDto("show2", "O que mais te atrai nesses eventos?",
                List.of("A musica/artista em si", "Estar com amigos", "A experiencia/producao do evento", "Sair da rotina"))
        )),
        Map.entry("carreira", List.of(
            new QuestionDto("carr1", "Na carreira, voce busca mais...",
                List.of("Crescimento rapido", "Estabilidade", "Proposito no que faz", "Liberdade e autonomia")),
            new QuestionDto("carr2", "Voce prefere crescer onde esta ou mudar de empresa/area?",
                List.of("Crescer onde estou", "Mudar de empresa", "Mudar de area completamente", "Ainda nao decidi"))
        )),
        Map.entry("empreendedorismo", List.of(
            new QuestionDto("emp1", "Sobre ter um negocio proprio, voce esta mais no estagio de...",
                List.of("So uma ideia, nada concreto ainda", "Ja tenho um plano", "Ja comecei algo pequeno", "Quero validar antes de investir tempo")),
            new QuestionDto("emp2", "Voce toparia abrir mao de estabilidade financeira por um tempo por isso?",
                List.of("Sim, sem problemas", "Sim, mas com um limite de tempo definido", "Prefiro manter renda fixa em paralelo", "Nao, de jeito nenhum agora"))
        )),
        Map.entry("saude", List.of(
            new QuestionDto("saude1", "Sobre saude/corpo, o que motiva mais?",
                List.of("Disciplina e resultado visivel", "Bem-estar mental", "Performance fisica", "Prevencao a longo prazo")),
            new QuestionDto("saude2", "Voce prefere treinar sozinho(a) ou com orientacao/grupo?",
                List.of("Sozinho(a)", "Com personal ou professor", "Em grupo/turma", "Ainda nao sei"))
        )),
        Map.entry("financeiro", List.of(
            new QuestionDto("fin1", "Sobre dinheiro, qual frase mais combina com voce?",
                List.of("Prefiro investir em experiencias", "Prefiro guardar e ter seguranca",
                        "Prefiro investir em mim (cursos, skills)", "Ainda nao penso muito nisso"))
        )),
        Map.entry("aprendizado", List.of(
            new QuestionDto("apr1", "Aprender algo novo, pra voce, e mais sobre...",
                List.of("Abrir portas profissionais", "Satisfacao pessoal", "Curiosidade pura", "Necessidade imediata")),
            new QuestionDto("apr2", "Voce aprende melhor...",
                List.of("Sozinho(a), no seu ritmo", "Em curso estruturado com prazo", "Na pratica, fazendo", "Com um mentor/professor"))
        )),
        Map.entry("relacionamento", List.of(
            new QuestionDto("rel1", "Sobre relacoes, o que precisa mais de atencao agora?",
                List.of("Fazer novas amizades", "Fortalecer relacoes que ja tenho", "Vida amorosa", "Relacoes de familia"))
        )),
        Map.entry("moradia", List.of(
            new QuestionDto("mor1", "Sobre mudar de casa/cidade, o que mais pesa?",
                List.of("Custo financeiro", "Deixar pessoas queridas", "Comecar do zero em outro lugar", "Nao sei se estou pronto(a)"))
        )),
        Map.entry("voluntariado", List.of(
            new QuestionDto("vol1", "No voluntariado/hobby, o que voce busca mais?",
                List.of("Sentir que ajudo alguem", "Aprender uma habilidade nova", "Fugir da rotina", "Conhecer gente nova"))
        ))
    );

    private final Map<String, List<String>> keywordMap = Map.ofEntries(
        Map.entry("viagem", List.of("viaj", "viagem", "mochil")),
        Map.entry("intercambio", List.of("intercambio", "exchange", "estudar fora", "morar fora")),
        Map.entry("show", List.of("show", "festival", "evento", "balad")),
        Map.entry("carreira", List.of("carreira", "emprego", "trabalho", "profiss")),
        Map.entry("empreendedorismo", List.of("empreende", "negocio proprio", "abrir empresa", "startup", "meu negocio")),
        Map.entry("saude", List.of("saude", "academia", "corpo", "fitness", "emagre")),
        Map.entry("financeiro", List.of("dinheiro", "financ", "investir", "poupar")),
        Map.entry("aprendizado", List.of("aprender", "curso", "estudar", "faculdade", "idioma")),
        Map.entry("relacionamento", List.of("namoro", "relacionamento", "amizade", "familia")),
        Map.entry("moradia", List.of("mudar de casa", "mudar de cidade", "morar sozinho", "mudanca")),
        Map.entry("voluntariado", List.of("voluntari", "hobby", "passatempo"))
    );

    private final AiService aiService;

    public QuestionnaireService(AiService aiService) {
        this.aiService = aiService;
    }

    public List<QuestionDto> pickQuestions(List<String> goals) {
        List<QuestionDto> result = new ArrayList<>(coreGeneral);

        // Pergunta dinamica: usa as proprias vontades da pessoa como opcoes de resposta.
        // So faz sentido perguntar isso quando ha mais de uma vontade pra comparar.
        if (goals.size() > 1) {
            List<String> beliefOptions = new ArrayList<>(goals);
            beliefOptions.add("Nenhuma me convence mais que as outras agora");
            result.add(new QuestionDto("g_belief",
                "Entre essas vontades, qual voce sente, no fundo, que e a certa pra voce agora?", beliefOptions));
        }

        try {
            // Perguntas especificas para a combinacao exata de vontades, geradas pela IA.
            List<QuestionDto> specific = aiService.generateQuestions(goals);
            if (!specific.isEmpty()) {
                result.addAll(specific);
                return result.size() > 16 ? result.subList(0, 16) : result;
            }
        } catch (Exception e) {
            // Se a IA falhar (ex: sem chave configurada, fora do ar), cai no banco fixo por categoria
            // para o questionario nunca ficar so com o nucleo generico.
        }

        String text = String.join(" ", goals).toLowerCase();
        for (Map.Entry<String, List<String>> entry : keywordMap.entrySet()) {
            boolean matched = entry.getValue().stream().anyMatch(text::contains);
            if (matched) {
                result.addAll(byCategory.getOrDefault(entry.getKey(), List.of()));
            }
        }

        // limite de 16 perguntas para nao cansar o usuario, mesmo com muitas categorias batendo
        return result.size() > 16 ? result.subList(0, 16) : result;
    }
}
