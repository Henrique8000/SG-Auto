package com.sgauto.app.repository.patio;

import com.sgauto.app.model.patio.TabelaPrecoPatio;
import com.sgauto.app.enums.CategoriaVeiculoPatio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TabelaPrecoPatioRepository extends JpaRepository<TabelaPrecoPatio, Long> {
    List<TabelaPrecoPatio> findByAtivoTrue();
    List<TabelaPrecoPatio> findByCategoriaAndAtivoTrue(CategoriaVeiculoPatio categoria);
    Optional<TabelaPrecoPatio> findByDescricao(String descricao);
}
