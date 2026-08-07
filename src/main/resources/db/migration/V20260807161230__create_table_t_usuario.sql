CREATE TABLE t_usuario (
                           id BIGSERIAL PRIMARY KEY,

                           usuario_login VARCHAR(50) NOT NULL UNIQUE,
                           usuario_senha_hash VARCHAR(255) NOT NULL,
                           usuario_nome_exibicao VARCHAR(150) NOT NULL,
                           usuario_email VARCHAR(150),

                           usuario_funcionario_id BIGINT REFERENCES t_funcionario(funcionario_id),
                           usuario_perfil_id BIGINT NOT NULL REFERENCES t_perfil_acesso(id),

                           usuario_ativo BOOLEAN NOT NULL DEFAULT TRUE,
                           usuario_deve_trocar_senha BOOLEAN NOT NULL DEFAULT TRUE,

                           usuario_tentativas_falhas SMALLINT NOT NULL DEFAULT 0,
                           usuario_bloqueado_ate TIMESTAMP,

                           usuario_ultimo_login TIMESTAMP,
                           usuario_data_criacao TIMESTAMP,
                           usuario_data_atualizacao TIMESTAMP
);