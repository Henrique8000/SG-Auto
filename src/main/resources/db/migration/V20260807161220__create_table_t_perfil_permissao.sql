CREATE TABLE t_perfil_permissao (
                                    perfil_id BIGINT NOT NULL REFERENCES t_perfil_acesso(id),
                                    permissao_id BIGINT NOT NULL REFERENCES t_permissao(id),
                                    PRIMARY KEY (perfil_id, permissao_id)
);

