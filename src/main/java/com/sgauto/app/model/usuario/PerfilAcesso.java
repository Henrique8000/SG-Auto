package com.sgauto.app.model.usuario;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_perfil_acesso")
public class PerfilAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "perfil_nome", nullable = false, unique = true, length = 50)
    private String nome;

    @Column(name = "perfil_descricao", length = 255)
    private String descricao;

    @Column(name = "perfil_protegido", nullable = false)
    private Boolean protegido = false;

    @Column(name = "perfil_ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "perfil_data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "perfil_data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "t_perfil_permissao",
            joinColumns = @JoinColumn(name = "perfil_id"),
            inverseJoinColumns = @JoinColumn(name = "permissao_id")
    )
    private Set<Permissao> permissoes = new HashSet<>();

    public PerfilAcesso() {}

    public PerfilAcesso(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    @PrePersist
    protected void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        this.dataCriacao = agora;
        this.dataAtualizacao = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Boolean getProtegido() { return protegido; }
    public void setProtegido(Boolean protegido) { this.protegido = protegido; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public Set<Permissao> getPermissoes() { return permissoes; }
    public void setPermissoes(Set<Permissao> permissoes) { this.permissoes = permissoes; }

    public boolean tem(String chavePermissao) {
        return permissoes.stream().anyMatch(p -> p.getChave().equals(chavePermissao));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PerfilAcesso that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
