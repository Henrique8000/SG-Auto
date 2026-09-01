-- Modelo de decomposição: a mão de obra é uma PARCELA do servico_valor,
-- não um valor somado. O total cobrado continua sendo servico_valor.
ALTER TABLE t_servico
    ADD COLUMN servico_valor_mao_de_obra NUMERIC(10,2) NOT NULL DEFAULT 0.00;