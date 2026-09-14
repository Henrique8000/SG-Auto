package com.sgauto.app.service.backup;

import com.sgauto.app.enums.TipoBackup;
import com.sgauto.app.service.backup.BackupHistoricoService;
import com.sgauto.app.service.backup.BackupService;
import com.sgauto.app.service.ConfigSistemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BackupScheduler {

    private static final Logger log = LoggerFactory.getLogger(BackupScheduler.class);

    private final ConfigSistemaService configSistemaService;
    private final BackupHistoricoService backupHistoricoService;
    private final BackupService backupService;

    public BackupScheduler(ConfigSistemaService configSistemaService,
                           BackupHistoricoService backupHistoricoService,
                           BackupService backupService) {
        this.configSistemaService = configSistemaService;
        this.backupHistoricoService = backupHistoricoService;
        this.backupService = backupService;
    }

    /**
     * Executa a cada 1 hora (3600000 milissegundos).
     * TESTES > 60000 para 1 minuto
     */
    @Scheduled(initialDelay = 10000, fixedDelay = 3600000)
    public void verificarEExecutarBackup() {
        log.debug("Verificando necessidade de backup automático...");

        try {
            boolean isAtivo = configSistemaService.isBackupAutomaticoAtivo();

            if (!isAtivo) {
                return;
            }

            boolean isAtrasado = backupHistoricoService.backupEstaAtrasado();

            if (isAtrasado) {
                log.info("Backup automático está atrasado. Iniciando rotina...");
                backupService.executarBackupPadrao(TipoBackup.AUTOMATICO);
            }

        } catch (Exception e) {
            log.error("Erro na rotina de verificação do Backup Automático", e);
        }
    }
}
