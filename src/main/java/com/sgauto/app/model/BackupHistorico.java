package com.sgauto.app.model;

import com.sgauto.app.enums.backup.StatusBackup;
import com.sgauto.app.enums.backup.TipoBackup;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Registro histórico de uma execução de backup (uma linha por tentativa,
 * sucesso ou falha). Tabela append-only: nunca é editada depois de criada,
 * por isso não tem data_atualizacao.
 */
@Entity
@Table(name = "t_backup_historico")
public class BackupHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "backup_tipo", nullable = false, length = 20)
    private TipoBackup tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "backup_status", nullable = false, length = 20)
    private StatusBackup status;

    @Column(name = "backup_destino", length = 500)
    private String destino;

    @Column(name = "backup_tamanho_bytes")
    private Long tamanhoBytes;

    @Column(name = "backup_mensagem_erro", columnDefinition = "TEXT")
    private String mensagemErro;

    @Column(name = "backup_data", nullable = false, updatable = false)
    private LocalDateTime data;

    public BackupHistorico() {
    }

    public BackupHistorico(TipoBackup tipo, StatusBackup status, String destino,
                           Long tamanhoBytes, String mensagemErro) {
        this.tipo = tipo;
        this.status = status;
        this.destino = destino;
        this.tamanhoBytes = tamanhoBytes;
        this.mensagemErro = mensagemErro;
    }

    @PrePersist
    protected void aoCriar() {
        this.data = LocalDateTime.now();
    }

    // ---------------------------------------------------------------------
    // Getters e Setters
    // ---------------------------------------------------------------------
    public Long getId() { return id; }

    public TipoBackup getTipo() { return tipo; }
    public void setTipo(TipoBackup tipo) { this.tipo = tipo; }

    public StatusBackup getStatus() { return status; }
    public void setStatus(StatusBackup status) { this.status = status; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public Long getTamanhoBytes() { return tamanhoBytes; }
    public void setTamanhoBytes(Long tamanhoBytes) { this.tamanhoBytes = tamanhoBytes; }

    public String getMensagemErro() { return mensagemErro; }
    public void setMensagemErro(String mensagemErro) { this.mensagemErro = mensagemErro; }

    public LocalDateTime getData() { return data; }
}
