-- Categorias do fechamento de caixa passam a refletir as origens que o sistema realmente gera.
--   Entradas = O.S. + pátio + avulso + suprimento
--   Saídas   = sangria + despesas (saídas avulsas)
-- As colunas de vendas de peças e serviços eram alimentadas por origens (VENDA_PECA, SERVICO)
-- que nenhum fluxo do sistema produz, por isso são removidas.
ALTER TABLE t_caixa
    ADD COLUMN caixa_total_os NUMERIC(10,2),
    ADD COLUMN caixa_total_patio NUMERIC(10,2),
    ADD COLUMN caixa_total_despesas NUMERIC(10,2),
DROP COLUMN caixa_total_vendas_pecas,
    DROP COLUMN caixa_total_servicos;