package com.sgauto.app.repository.financeiro;

import com.sgauto.app.enums.financeiro.StatusConta;
import com.sgauto.app.model.financeiro.ContaReceber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ContaReceberRepository extends JpaRepository<ContaReceber, Long>, JpaSpecificationExecutor<ContaReceber> {

    // JOIN FETCH evita o LazyInitializationException ao carregar o cliente e a categoria
    @Query("SELECT cr FROM ContaReceber cr LEFT JOIN FETCH cr.cliente LEFT JOIN FETCH cr.categoria")
    List<ContaReceber> findAllComRelacionamentos();

    @Query("SELECT cr FROM ContaReceber cr LEFT JOIN FETCH cr.cliente LEFT JOIN FETCH cr.categoria WHERE cr.status = :status")
    List<ContaReceber> findByStatusComRelacionamentos(StatusConta status);

    List<ContaReceber> findByStatus(StatusConta status);

    List<ContaReceber> findByClienteId(Long clienteId);

    List<ContaReceber> findByOrdemServicoId(Long ordemServicoId);

    List<ContaReceber> findByCategoriaId(Long categoriaId);

    List<ContaReceber> findByStatusAndDataVencimentoBefore(StatusConta status, LocalDate data);

    List<ContaReceber> findByDataVencimentoBetween(LocalDate inicio, LocalDate fim);
}