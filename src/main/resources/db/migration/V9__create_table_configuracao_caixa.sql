CREATE TABLE t_configuracao_caixa (
                                      id BIGINT PRIMARY KEY DEFAULT 1,

                                      config_modo_conferencia VARCHAR(20) NOT NULL DEFAULT 'OBRIGATORIA',
                                      config_data_atualizacao TIMESTAMP,

                                      CONSTRAINT chk_configuracao_singleton CHECK (id = 1)
);

INSERT INTO t_configuracao_caixa (id, config_modo_conferencia)
VALUES (1, 'OBRIGATORIA');