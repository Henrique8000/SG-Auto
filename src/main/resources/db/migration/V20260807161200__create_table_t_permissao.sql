CREATE TABLE t_permissao (
                             id BIGSERIAL PRIMARY KEY,
                             permissao_chave VARCHAR(50) NOT NULL UNIQUE,
                             permissao_descricao VARCHAR(150) NOT NULL,
                             permissao_modulo VARCHAR(50) NOT NULL
);
