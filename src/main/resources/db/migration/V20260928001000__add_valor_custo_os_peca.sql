-- Congela o custo unitário da peça no momento em que ela entra na O.S.,
-- assim como valor_unitario já congela o preço de venda.
-- Permite calcular a margem real mesmo que o custo da peça mude depois.
ALTER TABLE t_os_peca
    ADD COLUMN valor_custo_unitario NUMERIC(10,2);

-- Preenche linhas já existentes (bancos de dev com O.S. de teste)
UPDATE t_os_peca op
SET valor_custo_unitario = p.peca_preco_custo
    FROM t_peca p
WHERE p.id = op.peca_id;

ALTER TABLE t_os_peca
    ALTER COLUMN valor_custo_unitario SET NOT NULL;