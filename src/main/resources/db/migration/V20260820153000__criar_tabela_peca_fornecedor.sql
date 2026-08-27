CREATE TABLE t_peca_fornecedor (
                                   peca_id BIGINT NOT NULL,
                                   fornecedor_id BIGINT NOT NULL,
                                   PRIMARY KEY (peca_id, fornecedor_id),
                                   CONSTRAINT fk_pf_peca FOREIGN KEY (peca_id) REFERENCES t_peca (id) ON DELETE CASCADE,
                                   CONSTRAINT fk_pf_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES t_fornecedor (fornecedor_id) ON DELETE CASCADE
);