package com.sgauto.app.service;

import com.sgauto.app.enums.backup.ConfigChave;
import com.sgauto.app.model.ConfigSistema;
import com.sgauto.app.repository.ConfigSistemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Leitura e gravação das configurações do sistema (t_config).
 */
@Service
public class ConfigSistemaService {

    private final ConfigSistemaRepository configSistemaRepository;
    private static final Logger log = LoggerFactory.getLogger(ConfigSistemaService.class);

    public ConfigSistemaService(ConfigSistemaRepository configSistemaRepository) {
        this.configSistemaRepository = configSistemaRepository;
    }

    // TODO: implementar

    public String obterValor(ConfigChave chave) {
        if (chave == null) {
            log.warn("Tentativa de buscar uma configuração com chave nula. Retornando null.");
            return null;
        }

        return configSistemaRepository.findByChave(chave)
                .map(ConfigSistema::getValor)
                .orElse(null);
    }

    public void salvarValor(ConfigChave chave, String valor) {
        if (chave == null || valor == null) {
            log.warn("Tentativa de salvar uma configuração com chave ou valor nulo. Operação cancelada.");
            return;
        }

        if (valor.trim().isEmpty()) {
            log.warn("Atenção: Salvando um valor vazio (em branco) para a configuração de chave '{}'.", chave);
        }

        ConfigSistema config = configSistemaRepository.findByChave(chave)
                .orElse(new ConfigSistema(chave, ""));

        config.setValor(valor);
        configSistemaRepository.save(config);

        log.debug("Configuração '{}' salva/atualizada com sucesso.", chave);
    }

    public Optional<String> obterPastaBackupLocal() {
        ConfigSistema config = configSistemaRepository.findByChave(ConfigChave.BACKUP_PASTA_LOCAL)
                .orElseThrow(() -> new IllegalStateException("Configuração crítica BACKUP_PASTA_LOCAL não encontrada no banco de dados. Verifique as migrations."));

        String valor = config.getValor();

        if (valor == null || valor.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(valor);
    }

    public Optional<String> obterPastaBackupNuvem(){
        ConfigSistema config = configSistemaRepository.findByChave(ConfigChave.BACKUP_PASTA_NUVEM)
                .orElseThrow(() -> new IllegalStateException("Configuração crítica BACKUP_PASTA_NUVEM não encontrada no banco de dados. Verifique as migrations."));

        String valor = config.getValor();

        if (valor == null || valor.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(valor);
    }

    public boolean isBackupAutomaticoAtivo() {
        ConfigSistema config = configSistemaRepository.findByChave(ConfigChave.BACKUP_AUTOMATICO_ATIVO)
                .orElseThrow(() -> new IllegalStateException("Configuração crítica BACKUP_AUTOMATICO_ATIVO não encontrada."));

        return Boolean.parseBoolean(config.getValor());
    }

    public boolean isBackupAposFechamentoCaixaAtivo() {
        ConfigSistema config = configSistemaRepository.findByChave(ConfigChave.BACKUP_APOS_FECHAMENTO_CAIXA)
                .orElseThrow(() -> new IllegalStateException("Configuração crítica BACKUP_APOS_FECHAMENTO_CAIXA não encontrada. Verifique as migrations."));

        return Boolean.parseBoolean(config.getValor());
    }

    public int obterIntervaloDiasBackup() {
        ConfigSistema config = configSistemaRepository.findByChave(ConfigChave.BACKUP_INTERVALO_DIAS)
                .orElseThrow(() -> new IllegalStateException("Configuração crítica BACKUP_INTERVALO_DIAS não encontrada."));

        try {
            return Integer.parseInt(config.getValor());
        } catch (NumberFormatException e) {
            log.error("O valor do intervalo de backup não é um número válido: {}. Assumindo 1 dia por segurança.", config.getValor());
            return 1;
        }
    }
}
