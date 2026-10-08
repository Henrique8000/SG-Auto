package com.sgauto.app.dto.dashboard;

import com.sgauto.app.enums.financeiro.FormaPagamento;
import java.math.BigDecimal;

public record FaturamentoPorFormaPagamentoDTO(
        FormaPagamento formaPagamento,
        BigDecimal valor
) {
}
