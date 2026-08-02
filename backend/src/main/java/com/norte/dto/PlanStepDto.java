package com.norte.dto;

public class PlanStepDto {
    private String id;
    private String descricao;
    private boolean feito;

    public PlanStepDto() {}
    public PlanStepDto(String id, String descricao, boolean feito) {
        this.id = id;
        this.descricao = descricao;
        this.feito = feito;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public boolean isFeito() { return feito; }
    public void setFeito(boolean feito) { this.feito = feito; }
}
