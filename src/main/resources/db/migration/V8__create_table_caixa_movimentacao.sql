CREATE TABLE t_caixa_movimentacao (
                                      id BIGSERIAL PRIMARY KEY,

                                      movimentacao_caixa_id BIGINT NOT NULL REFERENCES t_caixa(id),

                                      movimentacao_tipo VARCHAR(20) NOT NULL,
                                      movimentacao_origem VARCHAR(30) NOT NULL,
                                      movimentacao_forma_pagamento VARCHAR(20),

                                      movimentacao_valor NUMERIC(10,2) NOT NULL,
                                      movimentacao_descricao VARCHAR(255),
                                      movimentacao_data TIMESTAMP NOT NULL,

                                      movimentacao_referencia_id BIGINT,

                                      movimentacao_cliente_id BIGINT,
                                      movimentacao_placa VARCHAR(10)
);

CREATE INDEX idx_caixa_movimentacao_caixa ON t_caixa_movimentacao(movimentacao_caixa_id);
CREATE INDEX idx_caixa_movimentacao_cliente ON t_caixa_movimentacao(movimentacao_cliente_id);