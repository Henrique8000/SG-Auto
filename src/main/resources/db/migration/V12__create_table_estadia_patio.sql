CREATE TABLE t_estadia_patio (
                                 id BIGSERIAL PRIMARY KEY,

                                 estadia_veiculo_id BIGINT NOT NULL,         -- soft reference (t_veiculo ainda não existe)
                                 estadia_cliente_id BIGINT NOT NULL,         -- soft reference (t_cliente ainda não existe/confirmado)
                                 estadia_ordem_servico_id BIGINT,            -- soft reference; nulo = entrada avulsa
                                 estadia_placa VARCHAR(10),                  -- redundância proposital, ver nota abaixo

                                 estadia_tarifa_id BIGINT NOT NULL REFERENCES t_tarifa_patio(id),
                                 estadia_motivo_id BIGINT NOT NULL REFERENCES t_motivo_estadia(id),

                                 estadia_data_entrada TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 estadia_data_saida TIMESTAMP,

                                 estadia_localizacao VARCHAR(50) NOT NULL,
                                 estadia_status VARCHAR(20) NOT NULL DEFAULT 'NO_PATIO',
                                 estadia_valor_total NUMERIC(10,2),

                                 CONSTRAINT chk_estadia_status CHECK (estadia_status IN ('NO_PATIO', 'FINALIZADO'))
);

CREATE INDEX idx_estadia_patio_status ON t_estadia_patio(estadia_status);
CREATE INDEX idx_estadia_patio_veiculo ON t_estadia_patio(estadia_veiculo_id);
CREATE INDEX idx_estadia_patio_cliente ON t_estadia_patio(estadia_cliente_id);
CREATE INDEX idx_estadia_patio_tarifa ON t_estadia_patio(estadia_tarifa_id);
CREATE INDEX idx_estadia_patio_os ON t_estadia_patio(estadia_ordem_servico_id);