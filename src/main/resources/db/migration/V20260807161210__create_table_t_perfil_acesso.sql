CREATE TABLE t_perfil_acesso (
                                 id BIGSERIAL PRIMARY KEY,
                                 perfil_nome VARCHAR(50) NOT NULL UNIQUE,
                                 perfil_descricao VARCHAR(255),
                                 perfil_protegido BOOLEAN NOT NULL DEFAULT FALSE,
                                 perfil_ativo BOOLEAN NOT NULL DEFAULT TRUE,
                                 perfil_data_criacao TIMESTAMP,
                                 perfil_data_atualizacao TIMESTAMP
);

INSERT INTO t_perfil_acesso (perfil_nome, perfil_descricao, perfil_protegido)
VALUES ('Administrador', 'Acesso completo ao sistema, incluindo gestão de usuários.', TRUE);

INSERT INTO t_perfil_acesso (perfil_nome, perfil_descricao, perfil_protegido)
VALUES ('Operador', 'Acesso padrão às operações rotineiras do sistema.', FALSE);