package com.sgauto.app.dto.dashboard;

import java.math.BigDecimal;

public record DashboardResumoDTO(
        BigDecimal faturamentoCaixaDia,
        BigDecimal faturamentoCaixaMes,
        BigDecimal faturamentoOsFinalizadasMes,
        long osAbertasTotal,
        long osAguardandoAprovacao,
        BigDecimal ticketMedio,
        long pecasEstoqueCritico,
        long veiculosNoPatio
) {
}
