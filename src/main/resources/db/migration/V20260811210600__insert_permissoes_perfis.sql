INSERT INTO t_perfil_permissao (perfil_id, permissao_id)
SELECT (SELECT id FROM t_perfil_acesso WHERE perfil_nome = 'Administrador'), id FROM t_permissao;

INSERT INTO t_perfil_permissao (perfil_id, permissao_id)
SELECT
    (SELECT id FROM t_perfil_acesso WHERE perfil_nome = 'Operador'),
    id
FROM t_permissao
WHERE permissao_chave IN (
    -- Cadastros Básicos (Sem exclusão)
                          'CLIENTE_VISUALIZAR', 'CLIENTE_CRIAR', 'CLIENTE_EDITAR',
                          'VEICULO_VISUALIZAR', 'VEICULO_CRIAR', 'VEICULO_EDITAR',

    -- Catálogo e Estoque (Apenas leitura do catálogo)
                          'PECA_VISUALIZAR',
                          'SERVICO_VISUALIZAR',

    -- Operacional (Sem poder de Cancelar OS)
                          'OS_VISUALIZAR', 'OS_CRIAR', 'OS_EDITAR', 'OS_APROVAR', 'OS_FINALIZAR',

    -- Pátio (Operação de pátio, sem alterar tarifas)
                          'PATIO_VISUALIZAR', 'PATIO_ENTRADA', 'PATIO_SAIDA',

    -- Financeiro (Operação de caixa básica, sem relatórios)
                          'CAIXA_VISUALIZAR', 'CAIXA_FECHAR', 'CAIXA_MOVIMENTAR'
    );