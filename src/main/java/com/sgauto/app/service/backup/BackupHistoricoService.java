package com.sgauto.app.service.backup;

import com.sgauto.app.enums.backup.StatusBackup;
import com.sgauto.app.enums.backup.TipoBackup;
import com.sgauto.app.model.BackupHistorico;
import com.sgauto.app.repository.BackupHistoricoRepository;
import com.sgauto.app.service.ConfigSistemaService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Consulta e registro do histórico de execuções de backup (t_backup_historico).
 *
 * Sugestões de métodos a implementar aqui:
 * - void registrarSucesso(TipoBackup tipo, String destino, long tamanhoBytes)
 * - void registrarFalha(TipoBackup tipo, String destino, String mensagemErro)
 * - Optional<BackupHistorico> obterUltimoBackup()
 * - List<BackupHistorico> listarUltimosRegistros()
 * - boolean backupEstaAtrasado(int intervaloDiasEsperado)
 */
@Service
public class BackupHistoricoService {

    private final BackupHistoricoRepository backupHistoricoRepository;
    private final ConfigSistemaService configSistemaService;

    public BackupHistoricoService(BackupHistoricoRepository backupHistoricoRepository, ConfigSistemaService configSistemaService) {
        this.backupHistoricoRepository = backupHistoricoRepository;
        this.configSistemaService = configSistemaService;
    }

    // TODO: implementar

    public void registrarSucesso(TipoBackup tipo, String destino, long tamanhoBytes) {
        BackupHistorico historico = new BackupHistorico(
                tipo,
                StatusBackup.SUCESSO,
                destino,
                tamanhoBytes,
                null
        );
        backupHistoricoRepository.save(historico);
    }

    public void registrarFalha(TipoBackup tipo, String destino, String mensagemErro) {
        BackupHistorico historico = new BackupHistorico(
                tipo,
                StatusBackup.FALHA,
                destino,
                null,
                mensagemErro
        );
        backupHistoricoRepository.save(historico);
    }

    public Optional<BackupHistorico> obterUltimoBackup() {
        return backupHistoricoRepository.findTopByStatusOrderByDataDesc(StatusBackup.SUCESSO);
    }

    public List<BackupHistorico> listarUltimosRegistros(){
        return backupHistoricoRepository.findTop20ByOrderByDataDesc();
    }

    public boolean backupEstaAtrasado() {
        int intervaloDiasEsperado = configSistemaService.obterIntervaloDiasBackup();

        Optional<BackupHistorico> ultimoBackup = obterUltimoBackup();

        if (ultimoBackup.isEmpty()) {
            return true;
        }

        LocalDateTime dataUltimo = ultimoBackup.get().getData();
        long diasPassados = java.time.temporal.ChronoUnit.DAYS.between(dataUltimo, LocalDateTime.now());

        return diasPassados >= intervaloDiasEsperado;
    }
}
