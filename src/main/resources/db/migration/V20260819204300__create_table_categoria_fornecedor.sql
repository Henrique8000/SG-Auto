CREATE TABLE t_fornecedor_categoria (
                             id BIGSERIAL PRIMARY KEY,
                             fornec_categoria_nome VARCHAR(100) NOT NULL UNIQUE,
                             fornec_categoria_descricao VARCHAR(255) NULL,
                             fornec_categoria_ativo BOOLEAN NOT NULL DEFAULT TRUE,
                             fornec_categoria_data_criacao TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             fornec_categoria_data_atualizacao TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);