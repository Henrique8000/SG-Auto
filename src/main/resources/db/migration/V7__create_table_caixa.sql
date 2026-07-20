CREATE TABLE t_caixa (
                         id BIGSERIAL PRIMARY KEY,

                         caixa_data_abertura TIMESTAMP NOT NULL,
                         caixa_data_fechamento TIMESTAMP,
                         caixa_usuario_abertura VARCHAR(100) NOT NULL,
                         caixa_usuario_fechamento VARCHAR(100),
                         caixa_status VARCHAR(20) NOT NULL DEFAULT 'ABERTO',

                         caixa_valor_abertura NUMERIC(10,2) NOT NULL DEFAULT 0,

                         caixa_total_entradas NUMERIC(10,2),
                         caixa_total_saidas NUMERIC(10,2),
                         caixa_total_vendas_pecas NUMERIC(10,2),
                         caixa_total_servicos NUMERIC(10,2),
                         caixa_total_avulso NUMERIC(10,2),
                         caixa_total_sangria NUMERIC(10,2),
                         caixa_total_suprimento NUMERIC(10,2),

                         caixa_total_dinheiro NUMERIC(10,2),
                         caixa_total_debito NUMERIC(10,2),
                         caixa_total_credito NUMERIC(10,2),
                         caixa_total_pix NUMERIC(10,2),

                         caixa_valor_esperado NUMERIC(10,2),
                         caixa_valor_contado NUMERIC(10,2),
                         caixa_diferenca NUMERIC(10,2),

                         caixa_modo_conferencia_usado VARCHAR(20),
                         caixa_justificativa_diferenca TEXT,

                         caixa_observacoes TEXT
);