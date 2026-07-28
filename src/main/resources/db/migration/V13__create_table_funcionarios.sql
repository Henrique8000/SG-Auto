CREATE TABLE t_funcionario (
    -- 1. Padrão Singular e Prefixo Limpo
                               funcionario_id                      BIGSERIAL PRIMARY KEY,
                               funcionario_matricula               VARCHAR(20)     NOT NULL,

    -- Dados pessoais
                               funcionario_nome_completo           VARCHAR(150)    NOT NULL,
                               funcionario_nome_social             VARCHAR(150),
                               funcionario_cpf                     VARCHAR(11)     NOT NULL,
                               funcionario_rg                      VARCHAR(20),
                               funcionario_data_nascimento         DATE,
                               funcionario_genero                  VARCHAR(20),

    -- Contato
                               funcionario_telefone_fixo           VARCHAR(15),
                               funcionario_celular                 VARCHAR(15)     NOT NULL,
                               funcionario_email                   VARCHAR(150),

    -- Endereço
                               funcionario_cep                     VARCHAR(9),
                               funcionario_logradouro              VARCHAR(150),
                               funcionario_numero                  VARCHAR(10),
                               funcionario_complemento             VARCHAR(100),
                               funcionario_bairro                  VARCHAR(100),
                               funcionario_cidade                  VARCHAR(100),
                               funcionario_estado                  CHAR(2),

    -- Dados profissionais / vínculo empregatício (Enums + CHECK)
                               funcionario_cargo                   VARCHAR(30)     NOT NULL,
                               funcionario_especialidade           VARCHAR(150),
                               funcionario_tipo_contrato           VARCHAR(20)     NOT NULL DEFAULT 'CLT',
                               funcionario_data_admissao           DATE            NOT NULL DEFAULT CURRENT_DATE,
                               funcionario_data_demissao           DATE,
                               funcionario_carga_horaria_semanal   SMALLINT        DEFAULT 44,

    -- Controle Operacional para Oficina
                               funcionario_exibe_em_os             BOOLEAN         NOT NULL DEFAULT TRUE,
                               funcionario_custo_hora              NUMERIC(10,2),

    -- Remuneração
                               funcionario_salario_base            NUMERIC(10,2),
                               funcionario_comissao_percentual     NUMERIC(5,2)    NOT NULL DEFAULT 0,

    -- Documentos profissionais (CNH p/ test-drive e manobra)
                               funcionario_numero_cnh              VARCHAR(20),
                               funcionario_categoria_cnh           VARCHAR(5),
                               funcionario_validade_cnh            DATE,

    -- Situação (Enum + CHECK) e Padrão Ativo (Boolean)
                               funcionario_status                  VARCHAR(20)     NOT NULL DEFAULT 'ATIVO',
                               funcionario_ativo                   BOOLEAN         NOT NULL DEFAULT TRUE,

                               funcionario_foto_url                VARCHAR(255),
                               funcionario_observacoes             TEXT,

    -- 2. Auditoria gerenciada exclusivamente via JPA (@PrePersist/@PreUpdate)
                               funcionario_data_criacao            TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               funcionario_data_atualizacao        TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               funcionario_removido_em TIMESTAMP WITH TIME ZONE,

    -- ---------------------- Constraints ----------------------
                               CONSTRAINT uq_funcionario_matricula UNIQUE (funcionario_matricula),
                               CONSTRAINT uq_funcionario_cpf       UNIQUE (funcionario_cpf),

                               CONSTRAINT chk_funcionario_cpf_formato
                                   CHECK (funcionario_cpf ~ '^[0-9]{11}$'),

    CONSTRAINT chk_funcionario_estado_formato
        CHECK (funcionario_estado IS NULL OR funcionario_estado ~ '^[A-Z]{2}$'),

    CONSTRAINT chk_funcionario_email_formato
        CHECK (funcionario_email IS NULL OR funcionario_email ~ '^[^@\s]+@[^@\s]+\.[^@\s]+$'),

    -- Enums do Sistema fechados via CHECK constraint
    CONSTRAINT chk_funcionario_cargo
        CHECK (funcionario_cargo IN (
            'MECANICO', 'ELETRICISTA', 'FUNILEIRO', 'PINTOR',
            'ATENDENTE', 'RECEPCIONISTA', 'CAIXA', 'GERENTE',
            'CONSULTOR_TECNICO', 'LAVADOR', 'AUXILIAR', 'ADMINISTRATIVO', 'OUTROS'
        )),

    CONSTRAINT chk_funcionario_tipo_contrato
        CHECK (funcionario_tipo_contrato IN ('CLT', 'PJ', 'ESTAGIO', 'TEMPORARIO', 'AUTONOMO')),

    CONSTRAINT chk_funcionario_status
        CHECK (funcionario_status IN ('ATIVO', 'INATIVO', 'FERIAS', 'AFASTADO', 'DEMITIDO')),

    CONSTRAINT chk_funcionario_comissao
        CHECK (funcionario_comissao_percentual >= 0 AND funcionario_comissao_percentual <= 100),

    CONSTRAINT chk_funcionario_salario
        CHECK (funcionario_salario_base IS NULL OR funcionario_salario_base >= 0),

    CONSTRAINT chk_funcionario_custo_hora
        CHECK (funcionario_custo_hora IS NULL OR funcionario_custo_hora >= 0),

    CONSTRAINT chk_funcionario_datas_admissao_demissao
        CHECK (funcionario_data_demissao IS NULL OR funcionario_data_demissao >= funcionario_data_admissao)
);

-- ---------------------------------------------------------------------
-- Índices
-- ---------------------------------------------------------------------

-- Índice para a consulta mais crítica do sistema: preencher combos na tela de O.S.
CREATE INDEX idx_funcionario_combo_os ON t_funcionario (funcionario_cargo, funcionario_status)
    WHERE funcionario_ativo = TRUE AND funcionario_exibe_em_os = TRUE;

CREATE INDEX idx_funcionario_nome ON t_funcionario (funcionario_nome_completo);

-- E-mail único somente entre registros ativos
CREATE UNIQUE INDEX uq_funcionario_email_ativo
    ON t_funcionario (funcionario_email)
    WHERE funcionario_email IS NOT NULL AND funcionario_ativo = TRUE;

-- ---------------------------------------------------------------------
-- Comentários
-- ---------------------------------------------------------------------
COMMENT ON TABLE  t_funcionario IS 'Funcionários do centro automotivo (gestão de RH e alocação operacional em O.S.)';
COMMENT ON COLUMN t_funcionario.funcionario_ativo IS 'Padrão booleano de exclusão lógica (substitui o soft delete por timestamp)';
COMMENT ON COLUMN t_funcionario.funcionario_exibe_em_os IS 'Controla se o funcionário aparece para seleção técnica na abertura de Ordem de Serviço';
COMMENT ON COLUMN t_funcionario.funcionario_custo_hora IS 'Custo hora interno para futuros cálculos de margem de lucro real por Ordem de Serviço';