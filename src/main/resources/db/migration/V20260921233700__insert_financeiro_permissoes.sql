INSERT INTO t_permissao (permissao_chave, permissao_descricao, permissao_modulo) VALUES
('CONTA_PAGAR_VISUALIZAR', 'Visualizar tela de CONTAS A PAGAR', 'Financeiro'),
('CONTA_RECEBER_VISUALIZAR', 'Visualizar tela de CONTAS A RECEBER', 'Financeiro'),
('CONTA_PAGAR_GERENCIAR', 'Cadastrar, editar e cancelar contas a pagar', 'Financeiro'),
('CONTA_PAGAR_BAIXAR', 'Registrar o pagamento de uma conta a pagar', 'Financeiro'),
('CONTA_RECEBER_GERENCIAR', 'Cadastrar, editar e cancelar contas a receber', 'Financeiro'),
('CONTA_RECEBER_BAIXAR', 'Registrar o recebimento de uma conta a receber', 'Financeiro'),
('CATEGORIA_CONTA_VISUALIZAR', 'Visualizar a tela de categorias financeiras', 'Financeiro'),
('CATEGORIA_CONTA_GERENCIAR', 'Editar e criar categorias financeiras', 'Financeiro'),
('FINANCEIRO_RELATORIOS', 'Acessar relatórios e exportações do financeiro', 'Financeiro');

INSERT INTO t_perfil_permissao (perfil_id, permissao_id)
SELECT (SELECT id FROM t_perfil_acesso WHERE perfil_nome = 'Administrador'), id
FROM t_permissao
WHERE permissao_modulo = 'Financeiro'
    ON CONFLICT DO NOTHING;