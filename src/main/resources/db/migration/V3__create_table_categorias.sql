CREATE TABLE t_categoria (
                             id BIGSERIAL PRIMARY KEY,
                             categoria_nome VARCHAR(100) NOT NULL UNIQUE,
                             categoria_descricao VARCHAR(255) NULL,
                             categoria_tipo VARCHAR(20) NOT NULL DEFAULT 'AMBOS',
                             categoria_ativo BOOLEAN NOT NULL DEFAULT TRUE,
                             categoria_data_criacao TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             categoria_data_atualizacao TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);