package com.sgauto.app.dto.patio;

import com.sgauto.app.enums.StatusEstadiaPatio;

import java.time.LocalDate;

public class PatioFiltroDTO {

    private String busca;
    private StatusEstadiaPatio status;
    private Long motivoId;
    private LocalDate dataEntradaInicio;
    private LocalDate dataEntradaFim;

    public String getBusca() { return busca; }
    public void setBusca(String busca) { this.busca = busca; }

    public StatusEstadiaPatio getStatus() { return status; }
    public void setStatus(StatusEstadiaPatio status) { this.status = status; }

    public Long getMotivoId() { return motivoId; }
    public void setMotivoId(Long motivoId) { this.motivoId = motivoId; }

    public LocalDate getDataEntradaInicio() { return dataEntradaInicio; }
    public void setDataEntradaInicio(LocalDate dataEntradaInicio) { this.dataEntradaInicio = dataEntradaInicio; }

    public LocalDate getDataEntradaFim() { return dataEntradaFim; }
    public void setDataEntradaFim(LocalDate dataEntradaFim) { this.dataEntradaFim = dataEntradaFim; }
}