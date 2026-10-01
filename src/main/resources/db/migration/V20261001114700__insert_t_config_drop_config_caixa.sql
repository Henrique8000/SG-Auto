INSERT INTO t_config (config_chave, config_valor, config_data_criacao, config_data_atualizacao)
VALUES ('CAIXA_MODO_CONFERENCIA', 'OBRIGATORIA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Adiciona a configuração para os tipos de pagamento que entram no cálculo do caixa
INSERT INTO t_config (config_chave, config_valor, config_data_criacao, config_data_atualizacao)
VALUES ('CAIXA_FORMAS_PAGAMENTO_FECHAMENTO', 'DINHEIRO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Exclui a tabela antiga
DROP TABLE t_configuracao_caixa;