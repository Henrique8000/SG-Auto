CREATE TABLE t_modelo (
                          id BIGSERIAL PRIMARY KEY,
                          modelo_nome VARCHAR(100) NOT NULL UNIQUE,
                          modelo_descricao VARCHAR(255),
                          modelo_ativo BOOLEAN NOT NULL DEFAULT TRUE,
                          modelo_data_criacao TIMESTAMP,
                          modelo_data_atualizacao TIMESTAMP
);