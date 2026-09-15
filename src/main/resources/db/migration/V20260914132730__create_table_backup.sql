CREATE TABLE t_backup_historico (
                                    id BIGSERIAL PRIMARY KEY,
                                    backup_tipo VARCHAR(20) NOT NULL,
                                    backup_status VARCHAR(20) NOT NULL,
                                    backup_destino VARCHAR(500),
                                    backup_tamanho_bytes BIGINT,
                                    backup_mensagem_erro TEXT,
                                    backup_data TIMESTAMP NOT NULL
);