package com.sgauto.app.repository;

import com.sgauto.app.enums.OrigemMovimentacao;
import com.sgauto.app.enums.TipoMovimentacao;
import com.sgauto.app.model.CaixaMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaixaMovimentacaoRepository extends JpaRepository<CaixaMovimentacao, Long> {
    List<CaixaMovimentacao> findByCaixaId(Long caixaId);
    List<CaixaMovimentacao> findByCaixaIdAndTipo(Long caixaId, TipoMovimentacao tipo);
    List<CaixaMovimentacao> findByCaixaIdAndOrigem(Long caixaId, OrigemMovimentacao origem);
}