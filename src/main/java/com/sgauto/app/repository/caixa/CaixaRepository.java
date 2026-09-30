package com.sgauto.app.repository.caixa;

import com.sgauto.app.enums.StatusCaixa;
import com.sgauto.app.model.caixa.Caixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CaixaRepository extends JpaRepository<Caixa, Long> {
    Optional<Caixa> findByStatus(StatusCaixa status);
    List<Caixa> findAllByStatus(StatusCaixa status);
    List<Caixa> findByDataAberturaBetween(LocalDateTime inicio, LocalDateTime fim);

    // Relatório diário: caixas fechados no intervalo [inicio, fim)
    @Query("SELECT c FROM Caixa c WHERE c.dataFechamento >= :inicio AND c.dataFechamento < :fim ORDER BY c.dataFechamento")
    List<Caixa> buscarFechadosNoPeriodo(@Param("inicio") LocalDateTime inicio,
                                        @Param("fim") LocalDateTime fim);
}