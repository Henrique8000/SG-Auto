package com.sgauto.app.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FaturamentoDiarioDTO(
        LocalDate data,
        BigDecimal valor
) {
}
