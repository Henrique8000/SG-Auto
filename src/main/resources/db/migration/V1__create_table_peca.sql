CREATE TABLE t_peca (
                        id BIGSERIAL PRIMARY KEY,
                        peca_codigo VARCHAR(50) NOT NULL UNIQUE,
                        peca_descricao VARCHAR(150) NOT NULL,
                        peca_preco_custo NUMERIC(10,2) NOT NULL DEFAULT 0,
                        peca_preco_venda NUMERIC(10,2) NOT NULL DEFAULT 0,
                        peca_quantidade_estoque INTEGER NOT NULL DEFAULT 0,
                        peca_estoque_minimo INTEGER NOT NULL DEFAULT 0
);