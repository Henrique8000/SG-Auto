CREATE TABLE t_estadia_patio (
                                 id BIGSERIAL PRIMARY KEY,

                                 estadia_veiculo_id BIGINT NOT NULL REFERENCES t_veiculo(id),
                                 estadia_cliente_id BIGINT NOT NULL REFERENCES t_cliente(id),
                                 estadia_ordem_servico_id BIGINT REFERENCES t_ordem_servico(id),
                                 estadia_placa VARCHAR(10),

                                 estadia_tarifa_id BIGINT NOT NULL REFERENCES t_tabela_preco_patio(id),
                                 estadia_motivo_id BIGINT NOT NULL REFERENCES t_motivo_estadia(id),

                                 estadia_data_entrada TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 estadia_data_saida TIMESTAMP,

                                 estadia_localizacao VARCHAR(50),
                                 estadia_status VARCHAR(20) NOT NULL DEFAULT 'NO_PATIO',
                                 estadia_valor_total NUMERIC(10,2),

                                 CONSTRAINT chk_estadia_status CHECK (estadia_status IN ('NO_PATIO', 'FINALIZADO'))
);

CREATE INDEX idx_estadia_patio_status ON t_estadia_patio(estadia_status);
CREATE INDEX idx_estadia_patio_veiculo ON t_estadia_patio(estadia_veiculo_id);
CREATE INDEX idx_estadia_patio_cliente ON t_estadia_patio(estadia_cliente_id);
CREATE INDEX idx_estadia_patio_tarifa ON t_estadia_patio(estadia_tarifa_id);
CREATE INDEX idx_estadia_patio_os ON t_estadia_patio(estadia_ordem_servico_id);