CREATE TABLE t_veiculo (
                           id BIGSERIAL PRIMARY KEY,
                           veiculo_cliente_id BIGINT NOT NULL REFERENCES t_cliente(id),
                           veiculo_placa VARCHAR(7) NOT NULL UNIQUE,
                           veiculo_marca VARCHAR(50) NOT NULL,
                           veiculo_modelo VARCHAR(100) NOT NULL,
                           veiculo_ano INTEGER,
                           veiculo_km INTEGER,
                           veiculo_ativo BOOLEAN NOT NULL DEFAULT TRUE,
                           veiculo_data_criacao TIMESTAMP,
                           veiculo_data_atualizacao TIMESTAMP
);

CREATE INDEX idx_veiculo_cliente ON t_veiculo(veiculo_cliente_id);
CREATE INDEX idx_veiculo_placa ON t_veiculo(veiculo_placa);