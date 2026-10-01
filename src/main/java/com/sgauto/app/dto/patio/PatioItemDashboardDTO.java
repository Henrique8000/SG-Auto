package com.sgauto.app.dto.patio;

import com.sgauto.app.enums.patio.StatusEstadiaPatio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PatioItemDashboardDTO {

    private final Long estadiaId;
    private final String placa;
    private final Long clienteId; // <-- Movido para cá e transformado em final
    private final String clienteNome;
    private final String motivoNome;
    private final Long ordemServicoId;
    private final LocalDateTime dataEntrada;
    private final LocalDateTime dataSaida;
    private final StatusEstadiaPatio status;
    private final BigDecimal valorEstimadoOuFinal;
    private final String localizacao;

    // Construtor ATUALIZADO recebendo o clienteId
    public PatioItemDashboardDTO(Long estadiaId, String placa, Long clienteId, String clienteNome, String motivoNome,
                                 Long ordemServicoId, LocalDateTime dataEntrada, LocalDateTime dataSaida,
                                 StatusEstadiaPatio status, BigDecimal valorEstimadoOuFinal, String localizacao) {
        this.estadiaId = estadiaId;
        this.placa = placa;
        this.clienteId = clienteId; // <-- Setando o valor
        this.clienteNome = clienteNome;
        this.motivoNome = motivoNome;
        this.ordemServicoId = ordemServicoId;
        this.dataEntrada = dataEntrada;
        this.dataSaida = dataSaida;
        this.status = status;
        this.valorEstimadoOuFinal = valorEstimadoOuFinal;
        this.localizacao = localizacao;
    }

    public Long getEstadiaId() { return estadiaId; }
    public String getPlaca() { return placa; }
    public Long getClienteId() { return clienteId; } // <-- Getter
    public String getClienteNome() { return clienteNome; }
    public String getMotivoNome() { return motivoNome; }
    public Long getOrdemServicoId() { return ordemServicoId; }
    public LocalDateTime getDataEntrada() { return dataEntrada; }
    public LocalDateTime getDataSaida() { return dataSaida; }
    public StatusEstadiaPatio getStatus() { return status; }
    public BigDecimal getValorEstimadoOuFinal() { return valorEstimadoOuFinal; }
    public String getLocalizacao() { return localizacao; }
}