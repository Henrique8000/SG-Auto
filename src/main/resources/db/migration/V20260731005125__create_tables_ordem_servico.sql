CREATE TABLE t_ordem_servico (
                                 id BIGSERIAL PRIMARY KEY,
                                 cliente_id BIGINT NOT NULL,
                                 veiculo_id BIGINT NOT NULL,
                                 funcionario_id BIGINT NOT NULL, -- Mecânico ou responsável técnico

    -- Controle de Fluxo e Estado
                                 status VARCHAR(50) NOT NULL DEFAULT 'ABERTA',
    -- Valores possíveis: ABERTA, VERIFICANDO_ORCAMENTO, EM_EXECUCAO, AGUARDANDO, CONCLUIDA, FINALIZADA, CANCELADA

    -- Textos e Justificativas
                                 sintomas_relatados TEXT,
                                 observacoes_internas TEXT,
                                 motivo_pausa VARCHAR(255), -- Preenchido quando o status for 'AGUARDANDO'

    -- Feature Futura
                                 ficar_no_patio BOOLEAN NOT NULL DEFAULT FALSE,

    -- Linha do Tempo
                                 data_abertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 data_previsao TIMESTAMP,
                                 data_conclusao TIMESTAMP,

    -- Consolidado Financeiro (Cacheados para evitar sum() em toda query)
                                 valor_total_pecas NUMERIC(10,2) NOT NULL DEFAULT 0.00,
                                 valor_total_servicos NUMERIC(10,2) NOT NULL DEFAULT 0.00,
                                 valor_desconto NUMERIC(10,2) NOT NULL DEFAULT 0.00,
                                 valor_total_os NUMERIC(10,2) NOT NULL DEFAULT 0.00, -- (Peças + Serviços) - Desconto

    -- Chaves Estrangeiras
                                 CONSTRAINT fk_os_cliente FOREIGN KEY (cliente_id) REFERENCES t_cliente(id),
                                 CONSTRAINT fk_os_veiculo FOREIGN KEY (veiculo_id) REFERENCES t_veiculo(id),
    -- ↓ A LINHA ABAIXO FOI CORRIGIDA ↓
                                 CONSTRAINT fk_os_funcionario FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(funcionario_id)
);


CREATE TABLE t_os_peca (
                           id BIGSERIAL PRIMARY KEY,
                           ordem_servico_id BIGINT NOT NULL,
                           peca_id BIGINT NOT NULL,

                           quantidade INTEGER NOT NULL,

    -- CONGELAMENTO DE VALOR: Copia o preço da peça no momento da inserção
                           valor_unitario NUMERIC(10,2) NOT NULL,
                           valor_total NUMERIC(10,2) NOT NULL, -- quantidade * valor_unitario

                           CONSTRAINT fk_os_peca_ordem FOREIGN KEY (ordem_servico_id) REFERENCES t_ordem_servico(id) ON DELETE CASCADE,
                           CONSTRAINT fk_os_peca_item FOREIGN KEY (peca_id) REFERENCES t_peca(id)
);


CREATE TABLE t_os_servico (
                              id BIGSERIAL PRIMARY KEY,
                              ordem_servico_id BIGINT NOT NULL,
                              servico_id BIGINT NOT NULL,

                              quantidade INTEGER NOT NULL DEFAULT 1,

    -- CONGELAMENTO DE VALOR: Copia o preço do serviço no momento da inserção
                              valor_unitario NUMERIC(10,2) NOT NULL,
                              valor_total NUMERIC(10,2) NOT NULL,

                              CONSTRAINT fk_os_servico_ordem FOREIGN KEY (ordem_servico_id) REFERENCES t_ordem_servico(id) ON DELETE CASCADE,
                              CONSTRAINT fk_os_servico_item FOREIGN KEY (servico_id) REFERENCES t_servico(id)
);


CREATE TABLE t_os_pagamento (
                                id BIGSERIAL PRIMARY KEY,
                                ordem_servico_id BIGINT NOT NULL,

                                data_pagamento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                forma_pagamento VARCHAR(50) NOT NULL, -- PIX, DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO
                                valor_pago NUMERIC(10,2) NOT NULL,

    -- Pode vincular diretamente à tabela do Caixa para rastreabilidade bidirecional
    -- caixa_movimentacao_id BIGINT,

                                CONSTRAINT fk_pagamento_os FOREIGN KEY (ordem_servico_id) REFERENCES t_ordem_servico(id) ON DELETE CASCADE
);



CREATE INDEX idx_os_status ON t_ordem_servico(status);
CREATE INDEX idx_os_cliente ON t_ordem_servico(cliente_id);
CREATE INDEX idx_os_veiculo ON t_ordem_servico(veiculo_id);