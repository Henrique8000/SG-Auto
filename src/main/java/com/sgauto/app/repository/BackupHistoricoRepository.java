package com.sgauto.app.repository;

import com.sgauto.app.enums.StatusBackup;
import com.sgauto.app.enums.TipoBackup;
import com.sgauto.app.model.BackupHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BackupHistoricoRepository extends JpaRepository<BackupHistorico, Long> {

    // Alimenta o card "Último backup: há X dias" do dashboard/configurações
    Optional<BackupHistorico> findTopByOrderByDataDesc();

    Optional<BackupHistorico> findTopByStatusOrderByDataDesc(StatusBackup status);

    List<BackupHistorico> findByTipoOrderByDataDesc(TipoBackup tipo);

    // Alimenta a tabela de histórico na tela de configurações (últimos N registros)
    List<BackupHistorico> findTop20ByOrderByDataDesc();

    // Útil para uma futura rotina de limpeza do próprio histórico (não dos arquivos)
    void deleteByDataBefore(LocalDateTime data);
}
