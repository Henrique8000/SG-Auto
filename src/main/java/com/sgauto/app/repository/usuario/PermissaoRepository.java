package com.sgauto.app.repository.usuario;

import com.sgauto.app.model.usuario.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissaoRepository extends JpaRepository<Permissao, Long> {
    List<Permissao> findByModulo(String modulo);
}
