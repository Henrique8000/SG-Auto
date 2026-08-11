package com.sgauto.app.repository.usuario;

import com.sgauto.app.model.usuario.Permissao;
import com.sgauto.app.model.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PermissaoRepository extends JpaRepository<Permissao, Long>, JpaSpecificationExecutor<Permissao> {
    List<Permissao> findByModulo(String modulo);
}
