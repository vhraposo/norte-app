package com.norte.dto;

import java.time.Instant;
import java.util.List;

public class RecommendationResponse {
    private Long id;
    private int version;
    private String titulo;
    private String porque;
    private List<String> pros;
    private List<String> contras;
    private String primeiroPasso;
    private List<Alternativa> alternativas;
    private String reflexao;
    private List<String> goalsSnapshot;
    private Boolean userAgrees;
    private String userComment;
    private Long planId;
    private Instant createdAt;

    public static class Alternativa {
        private String titulo;
        private String porqueNaoAgora;

        public Alternativa() {}
        public Alternativa(String titulo, String porqueNaoAgora) {
            this.titulo = titulo;
            this.porqueNaoAgora = porqueNaoAgora;
        }
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public String getPorqueNaoAgora() { return porqueNaoAgora; }
        public void setPorqueNaoAgora(String porqueNaoAgora) { this.porqueNaoAgora = porqueNaoAgora; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getPorque() { return porque; }
    public void setPorque(String porque) { this.porque = porque; }
    public List<String> getPros() { return pros; }
    public void setPros(List<String> pros) { this.pros = pros; }
    public List<String> getContras() { return contras; }
    public void setContras(List<String> contras) { this.contras = contras; }
    public String getPrimeiroPasso() { return primeiroPasso; }
    public void setPrimeiroPasso(String primeiroPasso) { this.primeiroPasso = primeiroPasso; }
    public List<Alternativa> getAlternativas() { return alternativas; }
    public void setAlternativas(List<Alternativa> alternativas) { this.alternativas = alternativas; }
    public String getReflexao() { return reflexao; }
    public void setReflexao(String reflexao) { this.reflexao = reflexao; }
    public List<String> getGoalsSnapshot() { return goalsSnapshot; }
    public void setGoalsSnapshot(List<String> goalsSnapshot) { this.goalsSnapshot = goalsSnapshot; }
    public Boolean getUserAgrees() { return userAgrees; }
    public void setUserAgrees(Boolean userAgrees) { this.userAgrees = userAgrees; }
    public String getUserComment() { return userComment; }
    public void setUserComment(String userComment) { this.userComment = userComment; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
