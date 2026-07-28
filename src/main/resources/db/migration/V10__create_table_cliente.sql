CREATE TABLE t_cliente (
                           id BIGSERIAL PRIMARY KEY,
                           cliente_tipo_pessoa VARCHAR(2) NOT NULL,
                           cliente_nome VARCHAR(150) NOT NULL,
                           cliente_documento VARCHAR(14) UNIQUE,
                           cliente_celular VARCHAR(11),
                           cliente_telefone VARCHAR(10),
                           cliente_email VARCHAR(150),
                           cliente_observacoes TEXT,
                           cliente_ativo BOOLEAN NOT NULL DEFAULT TRUE,
                           cliente_data_criacao TIMESTAMP,
                           cliente_data_atualizacao TIMESTAMP,

    -- exclusivos PF
                           cliente_rg VARCHAR(20),
                           cliente_data_nascimento DATE,

    -- exclusivos PJ
                           cliente_nome_fantasia VARCHAR(150),
                           cliente_inscricao_estadual VARCHAR(20),

    -- endereço (embeddable)
                           cliente_cep VARCHAR(8),
                           cliente_logradouro VARCHAR(150),
                           cliente_numero VARCHAR(10),
                           cliente_complemento VARCHAR(50),
                           cliente_bairro VARCHAR(100),
                           cliente_cidade VARCHAR(100),
                           cliente_uf CHAR(2)
);