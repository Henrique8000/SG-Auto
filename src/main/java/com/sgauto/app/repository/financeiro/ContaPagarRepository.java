package com.sgauto.app.repository.financeiro;

import com.sgauto.app.enums.financeiro.StatusConta;
import com.sgauto.app.model.financeiro.ContaPagar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ContaPagarRepository extends JpaRepository<ContaPagar, Long>, JpaSpecificationExecutor<ContaPagar> {

    @Query("SELECT cp FROM ContaPagar cp LEFT JOIN FETCH cp.fornecedor LEFT JOIN FETCH cp.categoria")
    List<ContaPagar> findAllComRelacionamentos();

    @Query("SELECT cp FROM ContaPagar cp LEFT JOIN FETCH cp.fornecedor LEFT JOIN FETCH cp.categoria WHERE cp.status = :status")
    List<ContaPagar> findByStatusComRelacionamentos(@Param("status") StatusConta status);

    List<ContaPagar> findByStatus(StatusConta status);
    List<ContaPagar> findByFornecedorId(Long fornecedorId);
    List<ContaPagar> findByCategoriaId(Long categoriaId);
    List<ContaPagar> findByStatusAndDataVencimentoBefore(StatusConta status, LocalDate data);
    List<ContaPagar> findByDataVencimentoBetween(LocalDate inicio, LocalDate fim);
}