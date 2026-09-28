package com.sgauto.app.repository.caixa;

import com.sgauto.app.enums.financeiro.StatusCaixa;
import com.sgauto.app.model.caixa.Caixa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CaixaRepository extends JpaRepository<Caixa, Long> {
    Optional<Caixa> findByStatus(StatusCaixa status);
    List<Caixa> findAllByStatus(StatusCaixa status);
    List<Caixa> findByDataAberturaBetween(LocalDateTime inicio, LocalDateTime fim);
}