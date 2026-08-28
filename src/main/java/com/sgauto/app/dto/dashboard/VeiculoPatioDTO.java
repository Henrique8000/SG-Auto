package com.sgauto.app.dto.dashboard;

public record VeiculoPatioDTO(
        Long id,
        String placa,
        String clienteNome,
        long diasNoPatio
) {
}
