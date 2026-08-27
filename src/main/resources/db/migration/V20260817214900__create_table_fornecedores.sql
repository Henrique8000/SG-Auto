CREATE TABLE t_fornecedor (
    -- Identificação Básica
                              fornecedor_id BIGSERIAL PRIMARY KEY,
                              fornecedor_tipo_pessoa VARCHAR(2) NOT NULL DEFAULT 'PJ', -- Permite diferenciar 'PF' (Pessoa Física) de 'PJ' (Pessoa Jurídica)
                              fornecedor_cpf_cnpj VARCHAR(20) UNIQUE NOT NULL,         -- Unicidade para evitar cadastros duplicados
                              fornecedor_razao_social VARCHAR(150) NOT NULL,           -- Nome oficial de registro
                              fornecedor_nome_fantasia VARCHAR(150),                   -- Nome comercial (como a loja é conhecida)
                              fornecedor_inscricao_estadual VARCHAR(50),               -- Necessário para emissão/recebimento de NFe
                              fornecedor_inscricao_municipal VARCHAR(50),              -- Necessário para serviços

    -- Informações de Contato
                              fornecedor_nome_contato VARCHAR(100),                    -- Nome do vendedor ou representante que atende sua empresa
                              fornecedor_telefone VARCHAR(20),                         -- Telefone fixo da empresa
                              fornecedor_celular VARCHAR(20),                          -- WhatsApp ou celular direto do representante
                              fornecedor_email VARCHAR(150),                           -- E-mail para envio de cotações e notas fiscais
                              fornecedor_site VARCHAR(150),                            -- Portal do fornecedor (opcional)

    -- Endereço Completo (Essencial para logística e faturamento)
                              fornecedor_cep VARCHAR(10),
                              fornecedor_logradouro VARCHAR(150),
                              fornecedor_numero VARCHAR(20),
                              fornecedor_complemento VARCHAR(100),
                              fornecedor_bairro VARCHAR(100),
                              fornecedor_cidade VARCHAR(100),
                              fornecedor_uf VARCHAR(2),

    -- Dados Operacionais e Controle
                              fornecedor_categoria VARCHAR(100),                       -- Ex: "Auto Peças", "Limpeza", "Tecnologia", "Ferramentas"
                              fornecedor_prazo_entrega_dias INT,                       -- Prazo médio que este fornecedor costuma levar para entregar
                              fornecedor_ativo BOOLEAN NOT NULL DEFAULT TRUE,          -- Soft delete (inativar fornecedor em vez de excluir)
                              fornecedor_observacoes TEXT,                             -- Campo livre para regras de frete, horário de atendimento, etc.

    -- Auditoria
                              fornecedor_criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              fornecedor_atualizado_em TIMESTAMP
);