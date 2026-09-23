package com.sgauto.app.repository.financeiro;

import com.sgauto.app.enums.StatusConta;
import com.sgauto.app.model.financeiro.ContaPagar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface ContaPagarRepository extends JpaRepository<ContaPagar, Long>, JpaSpecificationExecutor<ContaPagar> {

    List<ContaPagar> findByStatus(StatusConta status);

    List<ContaPagar> findByFornecedorId(Long fornecedorId);

    List<ContaPagar> findByCategoriaId(Long categoriaId);

    List<ContaPagar> findByStatusAndDataVencimentoBefore(StatusConta status, LocalDate data);

    List<ContaPagar> findByDataVencimentoBetween(LocalDate inicio, LocalDate fim);
}
