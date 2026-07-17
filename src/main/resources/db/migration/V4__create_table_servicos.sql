CREATE TABLE t_servico (
                           Id BIGSERIAL PRIMARY KEY,
                           servico_codigo VARCHAR(20) UNIQUE NOT NULL,
                           servico_nome VARCHAR(150) NOT NULL,
                           servico_categoria VARCHAR(50) NOT NULL,
                           servico_descricao VARCHAR(255) NULL,
                           servico_valor NUMERIC(10,2) NOT NULL DEFAULT 0.00,
                           servico_tempo_estimado_minutos INTEGER NOT NULL DEFAULT 60,
                           servico_garantia_dias INTEGER NOT NULL DEFAULT 90,
                           servico_comissao_porcentagem NUMERIC(5,2) NOT NULL DEFAULT 0.00,
                           servico_observacoes_tecnicas TEXT NULL,
                           servico_ativo BOOLEAN NOT NULL DEFAULT TRUE,
                           servico_data_criacao TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           servico_data_atualizacao TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);