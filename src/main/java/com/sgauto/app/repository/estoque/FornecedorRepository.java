package com.sgauto.app.repository.estoque;

import com.sgauto.app.model.estoque.Fornecedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long>, JpaSpecificationExecutor<Fornecedor> {
    Optional<Fornecedor> findByCpfCnpj(String cpfCnpj);

    boolean existsByCpfCnpj(String cpfCnpj);

    boolean existsByCpfCnpjAndIdNot(String cpfCnpj, Long id);

    List<Fornecedor> findByAtivoTrueOrderByRazaoSocialAsc();

    List<Fornecedor> findByCategoriaAndAtivoTrueOrderByRazaoSocialAsc(String categoria);

    long countByAtivo(boolean ativo);

    @Query("SELECT DISTINCT f.categoria FROM Fornecedor f WHERE f.ativo = true AND f.categoria IS NOT NULL ORDER BY f.categoria")
    List<String> findCategoriasAtivas();

    @Query("SELECT f FROM Fornecedor f WHERE f.ativo = :ativo AND " +
            "(LOWER(f.razaoSocial) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            " LOWER(f.nomeFantasia) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            " f.cpfCnpj LIKE CONCAT('%', :termo, '%'))")
    Page<Fornecedor> pesquisarFornecedores(@Param("termo") String termo,
                                           @Param("ativo") Boolean ativo,
                                           Pageable pageable);

    @Query("SELECT f FROM Fornecedor f WHERE f.ativo = :ativo " +
            "AND f.categoria = :categoria " +
            "AND (LOWER(f.razaoSocial) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "     LOWER(f.nomeFantasia) LIKE LOWER(CONCAT('%', :termo, '%')))")
    Page<Fornecedor> pesquisarPorCategoriaETermo(@Param("termo") String termo,
                                                 @Param("categoria") String categoria,
                                                 @Param("ativo") Boolean ativo,
                                                 Pageable pageable);
}
