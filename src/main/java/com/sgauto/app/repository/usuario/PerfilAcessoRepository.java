package com.sgauto.app.repository.usuario;

import com.sgauto.app.model.usuario.PerfilAcesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerfilAcessoRepository extends JpaRepository<PerfilAcesso, Long> {
    Optional<PerfilAcesso> findByNome(String nome);
    List<PerfilAcesso> findByAtivoTrue();
}
