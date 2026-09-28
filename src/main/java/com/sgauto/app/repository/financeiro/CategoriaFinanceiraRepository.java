package com.sgauto.app.repository.financeiro;

import com.sgauto.app.enums.financeiro.TipoCategoriaFinanceira;
import com.sgauto.app.model.financeiro.CategoriaFinanceira;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaFinanceiraRepository extends JpaRepository<CategoriaFinanceira, Long> {

    Page<CategoriaFinanceira> findByAtivoTrue(Pageable pageable);

    @Override
    Optional<CategoriaFinanceira> findById(Long Id);

    List<CategoriaFinanceira> findAllByAtivoTrue();

    Page<CategoriaFinanceira> findByTipoInAndAtivoTrue(List<TipoCategoriaFinanceira> tipos, Pageable pageable);

    boolean existsByNome(String nome);

    @Query("SELECT c FROM CategoriaFinanceira c WHERE c.ativo = :ativo " +
            "AND (:nome IS NULL OR c.nome ILIKE :nome) " +
            "AND (:tipo IS NULL OR c.tipo = :tipo)")
    Page<CategoriaFinanceira> buscarComFiltros(
            @Param("nome") String nome,
            @Param("tipo") TipoCategoriaFinanceira tipo,
            @Param("ativo") Boolean ativo,
            Pageable pageable
    );
}