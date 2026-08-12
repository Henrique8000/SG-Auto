INSERT INTO t_permissao (permissao_chave, permissao_descricao, permissao_modulo) VALUES
-- MÓDULO: USUÁRIOS E SEGURANÇA
('USUARIO_VISUALIZAR', 'Visualizar listagem e detalhes de usuários', 'Segurança'),
('USUARIO_CRIAR', 'Cadastrar novos usuários no sistema', 'Segurança'),
('USUARIO_EDITAR', 'Editar dados de usuários existentes', 'Segurança'),
('USUARIO_EXCLUIR', 'Desativar ou excluir usuários', 'Segurança'),
('PERFIL_VISUALIZAR', 'Visualizar perfis de acesso', 'Segurança'),
('PERFIL_GERENCIAR', 'Criar, editar e excluir perfis e suas permissões', 'Segurança'),

-- MÓDULO: CADASTROS BÁSICOS (CLIENTES E VEÍCULOS)
('CLIENTE_VISUALIZAR', 'Visualizar carteira de clientes', 'Cadastros'),
('CLIENTE_CRIAR', 'Cadastrar novos clientes', 'Cadastros'),
('CLIENTE_EDITAR', 'Atualizar informações de clientes', 'Cadastros'),
('CLIENTE_EXCLUIR', 'Excluir registros de clientes', 'Cadastros'),
('VEICULO_VISUALIZAR', 'Visualizar frota e veículos cadastrados', 'Cadastros'),
('VEICULO_CRIAR', 'Cadastrar novos veículos', 'Cadastros'),
('VEICULO_EDITAR', 'Atualizar dados de veículos', 'Cadastros'),
('VEICULO_EXCLUIR', 'Excluir veículos do sistema', 'Cadastros'),

-- MÓDULO: RECURSOS HUMANOS
('FUNCIONARIO_VISUALIZAR', 'Visualizar quadro de funcionários', 'RH'),
('FUNCIONARIO_CRIAR', 'Cadastrar novos funcionários', 'RH'),
('FUNCIONARIO_EDITAR', 'Editar dados e salários de funcionários', 'RH'),
('FUNCIONARIO_EXCLUIR', 'Demitir ou excluir funcionários', 'RH'),

-- MÓDULO: CATÁLOGO E ESTOQUE
('PECA_VISUALIZAR', 'Visualizar catálogo e saldo de peças', 'Estoque'),
('PECA_CRIAR', 'Cadastrar novas peças no estoque', 'Estoque'),
('PECA_EDITAR', 'Editar informações, custos e preços de peças', 'Estoque'),
('PECA_EXCLUIR', 'Excluir peças do catálogo', 'Estoque'),
('SERVICO_VISUALIZAR', 'Visualizar tabela de serviços prestados', 'Estoque'),
('SERVICO_GERENCIAR', 'Criar, editar e remover serviços e tempos padrão', 'Estoque'),
('CATEGORIA_GERENCIAR', 'Gerenciar categorias e modelos de veículos/peças', 'Estoque'),

-- MÓDULO: OPERACIONAL (ORDEM DE SERVIÇO)
('OS_VISUALIZAR', 'Visualizar listagem e histórico de Ordens de Serviço', 'Operacional'),
('OS_CRIAR', 'Abrir nova Ordem de Serviço (Orçamento)', 'Operacional'),
('OS_EDITAR', 'Adicionar peças e serviços em uma OS aberta', 'Operacional'),
('OS_APROVAR', 'Aprovar orçamento e iniciar execução da OS', 'Operacional'),
('OS_FINALIZAR', 'Finalizar OS e liberar para faturamento', 'Operacional'),
('OS_CANCELAR', 'Cancelar uma Ordem de Serviço', 'Operacional'),

-- MÓDULO: PÁTIO E ESTADIA
('PATIO_VISUALIZAR', 'Visualizar veículos atualmente no pátio', 'Pátio'),
('PATIO_ENTRADA', 'Registrar entrada de veículo no pátio', 'Pátio'),
('PATIO_SAIDA', 'Registrar saída e calcular estadia', 'Pátio'),
('PATIO_CONFIGURAR', 'Configurar tarifas e motivos de estadia', 'Pátio'),

-- MÓDULO: FINANCEIRO E CAIXA
('CAIXA_VISUALIZAR', 'Visualizar status e histórico de caixas', 'Financeiro'),
('CAIXA_FECHAR', 'Realizar o fechamento e conferência de caixa', 'Financeiro'),
('CAIXA_MOVIMENTAR', 'Lançar suprimentos, sangrias e avulsos', 'Financeiro'),
('FINANCEIRO_RELATORIOS', 'Acessar relatórios de faturamento e lucros', 'Financeiro'),

-- MÓDULO: FINANCEIRO E CAIXA
('CONFIGURACOES_VISUALIZAR', 'Visualizar tela de configurações', 'Sistema'),
('CONFIGURACOES_EDITAR', 'Alterar Configurações', 'Sistema');