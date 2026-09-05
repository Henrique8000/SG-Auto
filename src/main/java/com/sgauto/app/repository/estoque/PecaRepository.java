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
            SELECT new com.sgauto.app.dto.dashboard.PecaEstoqueCriticoDTO(
                p.id, p.descricao, p.quantidadeEstoque, p.estoqueMinimo
            )
            FROM Peca p
            WHERE p.quantidadeEstoque <= p.estoqueMinimo
            ORDER BY p.quantidadeEstoque ASC
            """)
    List<PecaEstoqueCriticoDTO> buscarAbaixoDoEstoqueMinimo();

    @Query("""
            SELECT COUNT(p)
            FROM Peca p
            WHERE p.quantidadeEstoque <= p.estoqueMinimo
            """)
    long contarPecasEstoqueCritico();
}