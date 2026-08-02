package com.norte.dto;

import java.util.Map;

public class AnswerSubmission {
    // mapa questionId -> resposta escolhida (texto da opcao)
    private Map<String, String> answers;

    public Map<String, String> getAnswers() { return answers; }
    public void setAnswers(Map<String, String> answers) { this.answers = answers; }
}
