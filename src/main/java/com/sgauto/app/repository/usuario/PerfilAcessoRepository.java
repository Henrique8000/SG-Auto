package com.sgauto.app.repository.usuario;

import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.model.usuario.PerfilAcesso;
import com.sgauto.app.model.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PerfilAcessoRepository extends JpaRepository<PerfilAcesso, Long>, JpaSpecificationExecutor<PerfilAcesso> {
    Optional<PerfilAcesso> findByNome(String nome);
    List<PerfilAcesso> findByAtivoTrue();
    @Query("SELECT CASE WHEN COUNT(pa) > 0 THEN true ELSE false END " +
            "FROM PerfilAcesso pa JOIN pa.permissoes p " +
            "WHERE pa.id = :perfilId AND p.chave = :permissaoChave")
    boolean perfilPossuiPermissao(@Param("perfilId") Long perfilId, @Param("permissaoChave") String chave);
}
