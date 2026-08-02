package com.norte.dto;

import java.time.Instant;
import java.util.List;

public class PlanResponse {
    private Long id;
    private int number;
    private String titulo;
    private List<PlanStepDto> passos;
    private String ordemSugerida;
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public List<PlanStepDto> getPassos() { return passos; }
    public void setPassos(List<PlanStepDto> passos) { this.passos = passos; }
    public String getOrdemSugerida() { return ordemSugerida; }
    public void setOrdemSugerida(String ordemSugerida) { this.ordemSugerida = ordemSugerida; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
