package com.sgauto.app.dto.patio;

import java.math.BigDecimal;

public class PatioResumoDashboardDTO {

    private final long quantidadeVeiculosNoPatio;
    private final BigDecimal valorTotalEstimadoEmAberto;
    private final double tempoMedioPermanenciaHoras;

    public PatioResumoDashboardDTO(long quantidadeVeiculosNoPatio, BigDecimal valorTotalEstimadoEmAberto,
                                   double tempoMedioPermanenciaHoras) {
        this.quantidadeVeiculosNoPatio = quantidadeVeiculosNoPatio;
        this.valorTotalEstimadoEmAberto = valorTotalEstimadoEmAberto;
        this.tempoMedioPermanenciaHoras = tempoMedioPermanenciaHoras;
    }

    public long getQuantidadeVeiculosNoPatio() { return quantidadeVeiculosNoPatio; }
    public BigDecimal getValorTotalEstimadoEmAberto() { return valorTotalEstimadoEmAberto; }
    public double getTempoMedioPermanenciaHoras() { return tempoMedioPermanenciaHoras; }
}