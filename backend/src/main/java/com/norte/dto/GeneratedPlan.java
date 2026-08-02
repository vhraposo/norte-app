package com.norte.dto;

import java.util.List;

// Saida crua da IA para o plano de acao (Diario de Norte) - usado internamente pelo AiService.
public class GeneratedPlan {
    private List<String> passosTexto;
    private String ordemSugerida;

    public List<String> getPassosTexto() { return passosTexto; }
    public void setPassosTexto(List<String> passosTexto) { this.passosTexto = passosTexto; }
    public String getOrdemSugerida() { return ordemSugerida; }
    public void setOrdemSugerida(String ordemSugerida) { this.ordemSugerida = ordemSugerida; }
}
