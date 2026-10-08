-- ==========================================
-- TABELA: CATEGORIA FINANCEIRA
-- ==========================================
CREATE TABLE t_categoria_financeira (
                                        id                              BIGSERIAL PRIMARY KEY,
                                        categoria_financeira_nome       VARCHAR(100) NOT NULL,
                                        categoria_financeira_tipo       VARCHAR(10) NOT NULL,
                                        categoria_financeira_ativo      BOOLEAN NOT NULL DEFAULT true,
                                        categoria_financeira_criado_em  TIMESTAMP NOT NULL DEFAULT now(),
                                        CONSTRAINT chk_categoria_financeira_tipo CHECK (categoria_financeira_tipo IN ('RECEITA', 'DESPESA', 'AMBOS')),
                                        CONSTRAINT uq_categoria_financeira_nome_tipo UNIQUE (categoria_financeira_nome, categoria_financeira_tipo)
);


-- ==========================================
-- TABELA: CONTAS A RECEBER
-- ==========================================
CREATE TABLE t_conta_receber (
                                 id                               BIGSERIAL PRIMARY KEY,
                                 conta_receber_descricao          VARCHAR(255) NOT NULL,
                                 conta_receber_categoria_id       BIGINT,
                                 conta_receber_cliente_id         BIGINT,
                                 conta_receber_ordem_servico_id   BIGINT,
                                 conta_receber_numero_parcela     INT NOT NULL DEFAULT 1,
                                 conta_receber_total_parcelas     INT NOT NULL DEFAULT 1,
                                 conta_receber_valor_original     NUMERIC(12, 2) NOT NULL,
                                 conta_receber_valor_desconto     NUMERIC(12, 2) NOT NULL DEFAULT 0,
                                 conta_receber_valor_juros        NUMERIC(12, 2) NOT NULL DEFAULT 0,
                                 conta_receber_valor_multa        NUMERIC(12, 2) NOT NULL DEFAULT 0,
                                 conta_receber_valor_recebido     NUMERIC(12, 2),
                                 conta_receber_data_vencimento    DATE NOT NULL,
                                 conta_receber_data_recebimento   DATE,
                                 conta_receber_status             VARCHAR(20) NOT NULL,
                                 conta_receber_forma_recebimento  VARCHAR(20),
                                 conta_receber_origem             VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
                                 conta_receber_observacoes        TEXT,
                                 conta_receber_criado_em          TIMESTAMP NOT NULL DEFAULT now(),
                                 conta_receber_atualizado_em      TIMESTAMP NOT NULL DEFAULT now(),
                                 CONSTRAINT fk_conta_receber_categoria FOREIGN KEY (conta_receber_categoria_id) REFERENCES t_categoria_financeira (id),
                                 CONSTRAINT fk_conta_receber_cliente FOREIGN KEY (conta_receber_cliente_id) REFERENCES t_cliente (id),
                                 CONSTRAINT fk_conta_receber_os FOREIGN KEY (conta_receber_ordem_servico_id) REFERENCES t_ordem_servico (id),
                                 CONSTRAINT chk_conta_receber_status CHECK (conta_receber_status IN ('PENDENTE', 'PARCIAL', 'PAGO', 'ATRASADO', 'CANCELADO'))
);

CREATE INDEX idx_conta_receber_vencimento ON t_conta_receber(conta_receber_data_vencimento);
CREATE INDEX idx_conta_receber_status ON t_conta_receber(conta_receber_status);
CREATE INDEX idx_conta_receber_cliente ON t_conta_receber(conta_receber_cliente_id);
CREATE INDEX idx_conta_receber_categoria ON t_conta_receber(conta_receber_categoria_id);

-- ==========================================
-- TABELA: CONTAS A PAGAR
-- ==========================================
CREATE TABLE t_conta_pagar (
                               id                             BIGSERIAL PRIMARY KEY,
                               conta_pagar_descricao          VARCHAR(255) NOT NULL,
                               conta_pagar_categoria_id       BIGINT,
                               conta_pagar_fornecedor_id      BIGINT,
                               conta_pagar_numero_parcela     INT NOT NULL DEFAULT 1,
                               conta_pagar_total_parcelas     INT NOT NULL DEFAULT 1,
                               conta_pagar_valor_original     NUMERIC(12, 2) NOT NULL,
                               conta_pagar_valor_desconto     NUMERIC(12, 2) NOT NULL DEFAULT 0,
                               conta_pagar_valor_juros        NUMERIC(12, 2) NOT NULL DEFAULT 0,
                               conta_pagar_valor_multa        NUMERIC(12, 2) NOT NULL DEFAULT 0,
                               conta_pagar_valor_pago         NUMERIC(12, 2),
                               conta_pagar_data_vencimento    DATE NOT NULL,
                               conta_pagar_data_pagamento     DATE,
                               conta_pagar_status             VARCHAR(20) NOT NULL,
                               conta_pagar_forma_pagamento    VARCHAR(20),
                               conta_pagar_origem             VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
                               conta_pagar_observacoes        TEXT,
                               conta_pagar_criado_em          TIMESTAMP NOT NULL DEFAULT now(),
                               conta_pagar_atualizado_em      TIMESTAMP NOT NULL DEFAULT now(),
                               CONSTRAINT fk_conta_pagar_categoria FOREIGN KEY (conta_pagar_categoria_id) REFERENCES t_categoria_financeira (id),
                               CONSTRAINT fk_conta_pagar_fornecedor FOREIGN KEY (conta_pagar_fornecedor_id) REFERENCES t_fornecedor (fornecedor_id),
                               CONSTRAINT chk_conta_pagar_status CHECK (conta_pagar_status IN ('PENDENTE', 'PARCIAL', 'PAGO', 'ATRASADO', 'CANCELADO'))
);

CREATE INDEX idx_conta_pagar_vencimento ON t_conta_pagar(conta_pagar_data_vencimento);
CREATE INDEX idx_conta_pagar_status ON t_conta_pagar(conta_pagar_status);
CREATE INDEX idx_conta_pagar_fornecedor ON t_conta_pagar(conta_pagar_fornecedor_id);
CREATE INDEX idx_conta_pagar_categoria ON t_conta_pagar(conta_pagar_categoria_id);