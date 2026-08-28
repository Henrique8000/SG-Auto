package com.sgauto.app.repository.estoque;

import com.sgauto.app.dto.dashboard.PecaEstoqueCriticoDTO;
import com.sgauto.app.model.estoque.Peca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PecaRepository extends JpaRepository<Peca, Long> {
    Optional<Peca> findByCodigo(String codigo);
    boolean existsByModelo(String modelo);

    @Query("""
            SELECT new com.sgauto.app.dto.PecaEstoqueCriticoDTO(p.id, p.nome, p.quantidade, p.estoqueMinimo)
            FROM Peca p
            WHERE p.ativo = true
            AND p.quantidade <= p.estoqueMinimo
            ORDER BY p.quantidade ASC
            """)
    List<PecaEstoqueCriticoDTO> buscarAbaixoDoEstoqueMinimo();

    @Query("""
            SELECT COUNT(p)
            FROM Peca p
            WHERE p.ativo = true
            AND p.quantidade <= p.estoqueMinimo
            """)
    long contarPecasEstoqueCritico();
}