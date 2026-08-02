package com.norte.entity;

import jakarta.persistence.*;
import java.time.Instant;

// "Diario de Norte" - plano de acao (checklist) gerado quando o usuario concorda com uma recomendacao.
@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recommendation_id")
    private Recommendation recommendation;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false, length = 500)
    private String titulo;

    // JSON: lista de {"id": "...", "descricao": "...", "feito": true/false}
    @Lob
    @Column(nullable = false)
    private String passosJson;

    @Lob
    private String ordemSugerida;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Recommendation getRecommendation() { return recommendation; }
    public void setRecommendation(Recommendation recommendation) { this.recommendation = recommendation; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getPassosJson() { return passosJson; }
    public void setPassosJson(String passosJson) { this.passosJson = passosJson; }
    public String getOrdemSugerida() { return ordemSugerida; }
    public void setOrdemSugerida(String ordemSugerida) { this.ordemSugerida = ordemSugerida; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
