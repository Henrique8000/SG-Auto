package com.sgauto.app.model;

import com.sgauto.app.enums.backup.ConfigChave;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Tabela genérica de configurações do sistema, no modelo chave/valor.
 * Cada linha representa uma configuração (ex: pasta de backup local).
 * O valor é sempre armazenado como texto; a conversão para o tipo real
 * (boolean, int, etc.) fica a cargo do Service que consome a chave.
 */
@Entity
@Table(name = "t_config")
public class ConfigSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "config_chave", nullable = false, unique = true, length = 50)
    private ConfigChave chave;

    @Column(name = "config_valor", length = 500)
    private String valor;

    @Column(name = "config_data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "config_data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    public ConfigSistema() {
    }

    public ConfigSistema(ConfigChave chave, String valor) {
        this.chave = chave;
        this.valor = valor;
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

    // ---------------------------------------------------------------------
    // Getters e Setters
    // ---------------------------------------------------------------------
    public Long getId() { return id; }

    public ConfigChave getChave() { return chave; }
    public void setChave(ConfigChave chave) { this.chave = chave; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
}
