-- Contas a pagar quitadas pelo caixa (origem CONTA_PAGAR) passam a ter categoria própria no fechamento.
-- Com isso as categorias voltam a fechar com os totais:
--   saídas = sangria + despesas + contas pagas
ALTER TABLE t_caixa ADD COLUMN caixa_total_conta_pagar NUMERIC(15,2) NOT NULL DEFAULT 0;