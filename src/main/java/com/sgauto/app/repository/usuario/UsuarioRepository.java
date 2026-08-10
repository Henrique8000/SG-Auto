package com.sgauto.app.repository.usuario;

import com.sgauto.app.model.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLogin(String login);
    boolean existsByLogin(String login);
    boolean existsByLoginAndIdNot(String login, Long id);
    List<Usuario> findByAtivoTrue();
    List<Usuario> findByPerfilId(Long perfilId);
    long countByPerfilIdAndAtivoTrue(Long perfilId);
}
