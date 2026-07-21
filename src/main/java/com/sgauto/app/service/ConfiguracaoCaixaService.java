package com.sgauto.app.service;

import com.sgauto.app.enums.ModoConferencia;
import com.sgauto.app.model.ConfiguracaoCaixa;
import com.sgauto.app.repository.ConfiguracaoCaixaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracaoCaixaService {

    private static final Long ID_SINGLETON = 1L;

    private final ConfiguracaoCaixaRepository configuracaoCaixaRepository;

    public ConfiguracaoCaixaService(ConfiguracaoCaixaRepository configuracaoCaixaRepository) {
        this.configuracaoCaixaRepository = configuracaoCaixaRepository;
    }

    @Transactional(readOnly = true)
    public ConfiguracaoCaixa buscarConfiguracao() {
        return configuracaoCaixaRepository.findById(ID_SINGLETON)
                .orElseThrow(() -> new IllegalStateException("Configuração do caixa não encontrada no banco de dados."));
    }

    @Transactional
    public ConfiguracaoCaixa atualizarModoConferencia(ModoConferencia novoModo) {
        ConfiguracaoCaixa config = configuracaoCaixaRepository.findById(ID_SINGLETON)
                .orElseGet(() -> {
                    ConfiguracaoCaixa novaConfig = new ConfiguracaoCaixa();
                    novaConfig.setId(ID_SINGLETON);
                    return novaConfig;
                });

        config.setModoConferencia(novoModo);

        return configuracaoCaixaRepository.save(config);
    }
}