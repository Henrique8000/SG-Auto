package com.sgauto.app.controller.dto.patio;

import com.sgauto.app.enums.StatusEstadiaPatio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PatioItemDashboardDTO {

    private final Long estadiaId;
    private final String placa;
    private final String clienteNome;
    private final Long ordemServicoId;
    private final LocalDateTime dataEntrada;
    private final LocalDateTime dataSaida;
    private final StatusEstadiaPatio status;
    private final BigDecimal valorEstimadoOuFinal;
    private final String localizacao;

    public PatioItemDashboardDTO(Long estadiaId, String placa, String clienteNome, Long ordemServicoId,
                                 LocalDateTime dataEntrada, LocalDateTime dataSaida, StatusEstadiaPatio status,
                                 BigDecimal valorEstimadoOuFinal, String localizacao) {
        this.estadiaId = estadiaId;
        this.placa = placa;
        this.clienteNome = clienteNome;
        this.ordemServicoId = ordemServicoId;
        this.dataEntrada = dataEntrada;
        this.dataSaida = dataSaida;
        this.status = status;
        this.valorEstimadoOuFinal = valorEstimadoOuFinal;
        this.localizacao = localizacao;
    }

    public Long getEstadiaId() { return estadiaId; }
    public String getPlaca() { return placa; }
    public String getClienteNome() { return clienteNome; }
    public Long getOrdemServicoId() { return ordemServicoId; }
    public LocalDateTime getDataEntrada() { return dataEntrada; }
    public LocalDateTime getDataSaida() { return dataSaida; }
    public StatusEstadiaPatio getStatus() { return status; }
    public BigDecimal getValorEstimadoOuFinal() { return valorEstimadoOuFinal; }
    public String getLocalizacao() { return localizacao; }
}