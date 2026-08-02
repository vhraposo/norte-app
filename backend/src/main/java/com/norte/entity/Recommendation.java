package com.norte.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private int version;

    @Lob
    @Column(nullable = false)
    private String goalsSnapshotJson;

    @Lob
    @Column(nullable = false)
    private String answersSnapshotJson;

    @Column(nullable = false, length = 500)
    private String titulo;

    @Lob
    private String porque;

    @Lob
    private String prosJson;

    @Lob
    private String contrasJson;

    @Lob
    private String primeiroPasso;

    @Lob
    private String alternativasJson;

    @Lob
    private String reflexao;

    private Boolean userAgrees;

    @Lob
    private String userComment;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Recommendation parent;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getGoalsSnapshotJson() { return goalsSnapshotJson; }
    public void setGoalsSnapshotJson(String goalsSnapshotJson) { this.goalsSnapshotJson = goalsSnapshotJson; }
    public String getAnswersSnapshotJson() { return answersSnapshotJson; }
    public void setAnswersSnapshotJson(String answersSnapshotJson) { this.answersSnapshotJson = answersSnapshotJson; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getPorque() { return porque; }
    public void setPorque(String porque) { this.porque = porque; }
    public String getProsJson() { return prosJson; }
    public void setProsJson(String prosJson) { this.prosJson = prosJson; }
    public String getContrasJson() { return contrasJson; }
    public void setContrasJson(String contrasJson) { this.contrasJson = contrasJson; }
    public String getPrimeiroPasso() { return primeiroPasso; }
    public void setPrimeiroPasso(String primeiroPasso) { this.primeiroPasso = primeiroPasso; }
    public String getAlternativasJson() { return alternativasJson; }
    public void setAlternativasJson(String alternativasJson) { this.alternativasJson = alternativasJson; }
    public String getReflexao() { return reflexao; }
    public void setReflexao(String reflexao) { this.reflexao = reflexao; }
    public Boolean getUserAgrees() { return userAgrees; }
    public void setUserAgrees(Boolean userAgrees) { this.userAgrees = userAgrees; }
    public String getUserComment() { return userComment; }
    public void setUserComment(String userComment) { this.userComment = userComment; }
    public Recommendation getParent() { return parent; }
    public void setParent(Recommendation parent) { this.parent = parent; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
