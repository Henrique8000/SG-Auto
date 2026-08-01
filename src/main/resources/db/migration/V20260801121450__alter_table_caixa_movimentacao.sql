ALTER TABLE t_caixa_movimentacao
    ADD CONSTRAINT fk_movimentacao_cliente
        FOREIGN KEY (movimentacao_cliente_id) REFERENCES t_cliente(id);