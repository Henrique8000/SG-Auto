-- Inserindo o Administrador Padrão
INSERT INTO t_usuario (
    usuario_login,
    usuario_senha_hash,
    usuario_nome_exibicao,
    usuario_perfil_id,
    usuario_ativo,
    usuario_deve_trocar_senha,
    usuario_data_criacao
) VALUES (
             'admin',
             '$2a$12$SZQKNoNslkSjeI0KdeG7NuHuIPyZhX/U.K1Z1nV2G7hf0KsTfP0lO', -- Hash gerado para 'admin123'
             'Administrador do Sistema',
             (SELECT id FROM t_perfil_acesso WHERE perfil_nome = 'Administrador'),
             TRUE,
             FALSE,
             CURRENT_TIMESTAMP
         );

-- Inserindo o Operador Padrão
INSERT INTO t_usuario (
    usuario_login,
    usuario_senha_hash,
    usuario_nome_exibicao,
    usuario_perfil_id,
    usuario_ativo,
    usuario_deve_trocar_senha,
    usuario_data_criacao
) VALUES (
             'operador',
             '$2a$12$1BcNNz8I/9mZeY/pMoxpM.f83X3bOOlCHJtWHDaC0gfaWNBWJrkFW', -- Hash gerado para 'operador'
             'Operador Padrão',
             (SELECT id FROM t_perfil_acesso WHERE perfil_nome = 'Operador'),
             TRUE,
             FALSE,
             CURRENT_TIMESTAMP
         );