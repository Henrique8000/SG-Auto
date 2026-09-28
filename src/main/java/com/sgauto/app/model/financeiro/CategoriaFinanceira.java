package com.sgauto.app.model.financeiro;

import com.sgauto.app.enums.financeiro.TipoCategoriaFinanceira;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_categoria_financeira")
public class CategoriaFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "categoria_financeira_nome", nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_financeira_tipo", nullable = false, length = 10)
    private TipoCategoriaFinanceira tipo;

    @Column(name = "categoria_financeira_ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "categoria_financeira_criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void aoPersistir() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public CategoriaFinanceira() {
    }

    public CategoriaFinanceira(String nome, TipoCategoriaFinanceira tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoCategoriaFinanceira getTipo() {
        return tipo;
    }

    public void setTipo(TipoCategoriaFinanceira tipo) {
        this.tipo = tipo;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoriaFinanceira that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
