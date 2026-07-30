CREATE TABLE t_motivo_estadia (
                                  id BIGSERIAL PRIMARY KEY,
                                  motivo_nome VARCHAR(100) NOT NULL UNIQUE,
                                  motivo_descricao VARCHAR(255),
                                  motivo_ativo BOOLEAN NOT NULL DEFAULT TRUE,
                                  motivo_protegido BOOLEAN NOT NULL DEFAULT FALSE,
                                  motivo_data_criacao TIMESTAMP,
                                  motivo_data_atualizacao TIMESTAMP
);

INSERT INTO t_motivo_estadia (motivo_nome, motivo_descricao, motivo_ativo, motivo_protegido)
VALUES ('Ordem de Serviço', 'Preenchido automaticamente quando o veículo entra no pátio via O.S.', TRUE, TRUE);