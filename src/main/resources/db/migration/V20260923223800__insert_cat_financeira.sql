INSERT INTO t_categoria_financeira (categoria_financeira_nome, categoria_financeira_tipo)
VALUES ('Receita Automática (Sistema)', 'RECEITA')
    ON CONFLICT (categoria_financeira_nome, categoria_financeira_tipo) DO NOTHING;