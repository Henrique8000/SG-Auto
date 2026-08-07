CREATE TABLE t_perfil_permissao (
                                    perfil_id BIGINT NOT NULL REFERENCES t_perfil_acesso(id),
                                    permissao_id BIGINT NOT NULL REFERENCES t_permissao(id),
                                    PRIMARY KEY (perfil_id, permissao_id)
);

INSERT INTO t_perfil_permissao (perfil_id, permissao_id)
SELECT (SELECT id FROM t_perfil_acesso WHERE perfil_nome = 'Administrador'), id FROM t_permissao;