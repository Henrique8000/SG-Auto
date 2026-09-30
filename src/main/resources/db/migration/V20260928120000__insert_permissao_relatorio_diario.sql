-- Permissão do relatório diário, concedida só ao Administrador
INSERT INTO t_permissao (permissao_chave, permissao_descricao, permissao_modulo)
VALUES ('RELATORIO_DIARIO_VISUALIZAR', 'Visualizar e exportar o relatório diário da oficina', 'Relatórios');

INSERT INTO t_perfil_permissao (perfil_id, permissao_id)
SELECT (SELECT id FROM t_perfil_acesso WHERE perfil_nome = 'Administrador'), id
FROM t_permissao
WHERE permissao_chave = 'RELATORIO_DIARIO_VISUALIZAR';