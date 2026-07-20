package com.sgauto.app.model;

import com.sgauto.app.enums.ModoConferencia;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_configuracao_caixa")
public class ConfiguracaoCaixa {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "config_modo_conferencia", nullable = false, length = 20)
    private ModoConferencia modoConferencia;

    @Column(name = "config_data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public ConfiguracaoCaixa() {}

    @PreUpdate
    protected void aoAtualizar() {
        dataAtualizacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ModoConferencia getModoConferencia() { return modoConferencia; }
    public void setModoConferencia(ModoConferencia modoConferencia) { this.modoConferencia = modoConferencia; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}