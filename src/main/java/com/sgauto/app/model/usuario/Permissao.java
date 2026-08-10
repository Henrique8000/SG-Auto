package com.sgauto.app.model.usuario;

import jakarta.persistence.*;

@Entity
@Table(name = "t_permissao")
public class Permissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permissao_chave", nullable = false, unique = true, length = 50)
    private String chave;

    @Column(name = "permissao_descricao", nullable = false, length = 150)
    private String descricao;

    @Column(name = "permissao_modulo", nullable = false, length = 50)
    private String modulo;

    public Permissao() {}

    public Long getId() { return id; }
    public String getChave() { return chave; }
    public String getDescricao() { return descricao; }
    public String getModulo() { return modulo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permissao that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}