package com.sgauto.app.dto.dashboard;

public record PecaEstoqueCriticoDTO(
        Long id,
        String nome,
        Integer quantidade,
        Integer estoqueMinimo
) {
}
