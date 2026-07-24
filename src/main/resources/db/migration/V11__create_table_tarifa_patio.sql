CREATE TABLE t_tabela_preco_patio (
                                id BIGSERIAL PRIMARY KEY,
                                tpreco_descricao VARCHAR(100) NOT NULL,
                                tpreco_categoria VARCHAR(30) NOT NULL,
                                tpreco_valor_diaria NUMERIC(10,2) NOT NULL DEFAULT 0,
                                tpreco_dias_carencia INTEGER NOT NULL DEFAULT 0,
                                tpreco_ativo BOOLEAN NOT NULL DEFAULT TRUE,
                                tpreco_data_criacao TIMESTAMP,

                                CONSTRAINT chk_tarifa_categoria CHECK (tarifa_categoria IN ('MOTO', 'PASSEIO', 'SUV_CAMINHONETE', 'PESADO', 'OUTROS'))
);