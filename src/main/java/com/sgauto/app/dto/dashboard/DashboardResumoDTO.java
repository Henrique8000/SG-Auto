package com.sgauto.app.dto.dashboard;

import java.math.BigDecimal;

public record DashboardResumoDTO(
        BigDecimal faturamentoDia,
        BigDecimal faturamentoMes,
        long osAbertasTotal,
        long osAguardandoAprovacao,
        BigDecimal ticketMedio,
        long pecasEstoqueCritico,
        long veiculosNoPatio
) {
}
