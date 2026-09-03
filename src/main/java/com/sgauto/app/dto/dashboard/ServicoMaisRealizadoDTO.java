package com.sgauto.app.dto.dashboard;

public record ServicoMaisRealizadoDTO(
        Long servicoId,
        String servicoNome,
        long quantidade
) {
}
