package com.sgauto.app.dto.dashboard;

import java.math.BigDecimal;

public record ComissaoFuncionarioDTO(
        Long funcionarioId,
        String funcionarioNome,
        BigDecimal totalComissao
) {
}
