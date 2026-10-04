-- Linha do tempo da O.S.:
--   data_conclusao    -> fim do trabalho técnico (já existia)
--   data_finalizacao  -> O.S. paga e encerrada (status FINALIZADA)
--   data_cancelamento -> O.S. cancelada (status CANCELADA)
ALTER TABLE t_ordem_servico
    ADD COLUMN data_finalizacao TIMESTAMP,
    ADD COLUMN data_cancelamento TIMESTAMP;