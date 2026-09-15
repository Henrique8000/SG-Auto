CREATE TABLE t_config (
                          id BIGSERIAL PRIMARY KEY,
                          config_chave VARCHAR(50) NOT NULL UNIQUE,
                          config_valor VARCHAR(500),
                          config_data_criacao TIMESTAMP NOT NULL,
                          config_data_atualizacao TIMESTAMP NOT NULL
);